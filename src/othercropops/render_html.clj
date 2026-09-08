(ns othercropops.render-html
  "Build-time HTML renderer for `docs/samples/operator-console.html`.

  Closes flagship checklist item 2 (com-junkawasaki/root ADR-2607189300,
  Wave3 rollout): this repo previously had NO demo page generator. This
  namespace drives the REAL actor stack (`othercropops.operation` ->
  `othercropops.governor` -> `othercropops.store`) through a scenario
  adapted from this repo's own `othercropops.sim` demo driver and the
  hard-hold / always-escalate cases covered by `othercropops.governor`
  tests, rendered deterministically -- no invented numbers, no timestamps
  in the page content, byte-identical across reruns against the same seed.

  Shape mirrors `applianceshop.render-html` (cloud-itonami-isic-9522 REF)
  and the vegops/cerealops crop-ops family. This vertical's
  `othercropops.operation/build` is still the synchronous stub (langgraph
  StateGraph wiring is deferred per its docstring / README), so the
  renderer invokes that real function interface directly rather than
  `langgraph.graph/run*` -- every disposition / HARD-hold rule / basis
  still comes from the live governor, not a hand-typed copy.

  Usage: `clojure -M:dev:render-html [out-file]`
  (default `docs/samples/operator-console.html`)."
  (:require [jp-go-dds.skin]
            [kotoba.lang.text :as str]
            [othercropops.store :as store]
            [othercropops.operation :as op]))

(def ^:private operator
  {:actor-id "other-crop-ops-01" :role :farm-operator :phase :phase-3})

(defn- normalize-fact
  "Advisor traces use `:field-id`; disposition facts use `:subject`.
  Normalize so the ledger renderer can key every fact by subject."
  [f]
  (cond-> f
    (and (nil? (:subject f)) (:field-id f))
    (assoc :subject (:field-id f))))

(defn- exec!
  "Invoke the real OperationActor once and append its audit facts to
  `ledger-atom`. Returns the run result map."
  [actor ledger-atom request]
  (let [result (actor request operator)]
    (doseq [f (:audit result)]
      (swap! ledger-atom conj (normalize-fact f)))
    result))

(defn run-demo!
  "Runs a freshly seeded store through a scenario mixing every disposition
  this actor can reach under phase-3:

  - field-001 logs a clean planting record (phase-3 auto-commit when
    Governor is clean)
  - field-001 schedules a field operation (phase-3 auto-commit)
  - field-001 flags a crop-health concern (ALWAYS escalates -- stays
    awaiting human approval; sync stub has no resume path yet)
  - field-001 orders seed supplies over the category cost threshold
    (escalates -- awaiting human approval)
  - field-999 (UNREGISTERED) receives a log-field-record attempt which
    HARD-holds on `:field-not-registered` before any human
  - field-001 receives a log-field-record with non-positive acreage which
    HARD-holds on `:field-record-invalid`

  Every id/op/value is from othercropops.sim / othercropops.governor /
  othercropops.store / othercropops.facts -- no invented values. Returns
  `{:db store :ledger [facts...]}` -- every field read by `render` is
  real governor/operation output."
  []
  (let [db (store/mem-store
            {:initial-fields
             {"field-001"
              {:id "field-001"
               :name "Test Farm South Field"
               :crop "cut-flower"}}})
        actor (op/build db)
        ledger (atom [])]
    (exec! actor ledger
           {:op :log-field-record
            :field-id "field-001"
            :acreage 8
            :crop "cut-flower"
            :record-type "planting"})

    (exec! actor ledger
           {:op :schedule-field-operation
            :field-id "field-001"
            :operation-type "irrigation"
            :requested-date "2026-08-15"
            :reason "routine-schedule"})

    (exec! actor ledger
           {:op :flag-crop-health-concern
            :field-id "field-001"
            :concern "aphid-suspected"})

    (exec! actor ledger
           {:op :order-supplies
            :field-id "field-001"
            :category "seed"
            :cost 900})

    ;; HARD hold #1: unregistered field
    (exec! actor ledger
           {:op :log-field-record
            :field-id "field-999"
            :acreage 50
            :crop "fodder-corn"
            :record-type "planting"})

    ;; HARD hold #2: non-positive acreage on a registered field
    (exec! actor ledger
           {:op :log-field-record
            :field-id "field-001"
            :acreage 0
            :crop "cut-flower"
            :record-type "planting"})

    {:db db :ledger @ledger}))

;; ----------------------------- rendering -----------------------------

(defn- esc [v]
  (-> (str v)
      (str/replace "&" "&amp;")
      (str/replace "<" "&lt;")
      (str/replace ">" "&gt;")))

(defn- last-fact-for [ledger field-id]
  (last (filter #(= (:subject %) field-id) ledger)))

(defn- status-cell [ledger field-id]
  (let [f (last-fact-for ledger field-id)]
    (cond
      (nil? f) "<span class=\"muted\">no activity</span>"
      (= :committed (:t f)) "<span class=\"ok\">committed</span>"
      (= :approval-granted (:t f)) "<span class=\"ok\">approved &amp; committed</span>"
      (= :governor-hold (:t f))
      (let [rule (-> f :basis first)]
        (str "<span class=\"critical\">HARD hold &middot; " (esc (name (or rule :unknown))) "</span>"))
      (= :approval-requested (:t f)) "<span class=\"warn\">awaiting approval</span>"
      (= :advisor-proposal (:t f))
      (str "<span class=\"muted\">proposal (" (esc (name (or (:op f) :n-a))) ")</span>")
      :else "<span class=\"muted\">in progress</span>")))

(defn- ledger-row [{:keys [t op subject disposition basis]}]
  (format "        <tr><td>%s</td><td><code>%s</code></td><td>%s</td><td>%s</td></tr>"
          (esc (name t)) (esc (name (or op :n-a))) (esc (or subject ""))
          (esc (or (some->> basis (map name) (str/join ", "))
                   (some-> disposition name) ""))))

(def ^:private action-gate-rows
  ;; Static description of this actor's own closed op contract
  ;; (README Ops, othercropops.governor / othercropops.phase) --
  ;; documentation of fixed behavior, not runtime telemetry.
  ["        <tr><td><code>:log-field-record</code></td><td><span class=\"ok\">phase-3 auto-commit when clean + registered field; HARD hold on non-positive acreage</span></td></tr>"
   "        <tr><td><code>:schedule-field-operation</code></td><td><span class=\"ok\">phase-3 auto-commit when clean + registered field</span></td></tr>"
   "        <tr><td><code>:flag-crop-health-concern</code></td><td><span class=\"warn\">ALWAYS human approval (crop safety) · never auto at any phase</span></td></tr>"
   "        <tr><td><code>:order-supplies</code></td><td><span class=\"warn\">human approval over category cost threshold (seed/fertilizer 500, equipment 1000)</span></td></tr>"
   "        <tr><td><code>:operate-field-equipment</code></td><td><span class=\"critical\">HARD hold forever · farmer exclusive authority</span></td></tr>"
   "        <tr><td><code>:finalize-pesticide-application</code></td><td><span class=\"critical\">HARD hold forever · agronomist/farmer exclusive authority</span></td></tr>"])

(defn render
  "Renders the full operator-console.html document from a `run-demo!`
  result (or any map with `:db` store + `:ledger` fact vector)."
  [{:keys [db ledger]}]
  (let [field-001 (store/registered-field db "field-001")
        ledger-rows (str/join "\n" (map ledger-row ledger))
        hard-holds (filter #(= :governor-hold (:t %)) ledger)]
    (str
     "<html><head><meta charset=\"utf-8\"><title>cloud-itonami-isic-0119 &middot; other non-perennial crop ops</title><style>"
     (jp-go-dds.skin/dds+skin)
     "</style></head><body>\n"
     "<header class=\"bar\">\n"
     "  <h1>Other non-perennial crop growing ops (ISIC 0119) — Operator Console</h1>\n"
     "  <span class=\"badge\">read-only sample · governor-gated · crop-health / over-threshold supply always human-approved · "
     (count hard-holds) " HARD hold(s) this run</span>\n"
     "</header>\n"
     "<main>\n"
     "  <section class=\"card\">\n"
     "    <h2>Scenario fields</h2>\n"
     "    <p class=\"muted\">Demo snapshot — build-time-generated from <code>othercropops.store</code> via <code>othercropops.render-html</code> (<code>clojure -M:dev:render-html</code>), regenerated nightly. No invented data.</p>\n"
     "    <table>\n"
     "      <thead><tr><th>Field</th><th>Name</th><th>Crop</th><th>Last op status</th></tr></thead>\n"
     "      <tbody>\n"
     "        <tr><td>" (esc (:id field-001)) "</td><td>" (esc (:name field-001)) "</td><td>" (esc (:crop field-001))
     "</td><td>" (status-cell ledger "field-001") "</td></tr>\n"
     "        <tr><td>field-999</td><td class=\"muted\">(unregistered)</td><td class=\"muted\">—</td><td>"
     (status-cell ledger "field-999") "</td></tr>\n"
     "      </tbody>\n"
     "    </table>\n"
     "  </section>\n"
     "  <section class=\"card\">\n"
     "    <h2>Action gate (Field Operations Governor)</h2>\n"
     "    <p class=\"muted\">HARD holds cannot be overridden. Unregistered fields and non-positive acreage are rejected before any human; direct equipment operation and pesticide-application finalization are permanently blocked; crop-health concerns and over-threshold supply orders always escalate.</p>\n"
     "    <table>\n"
     "      <thead><tr><th>Op</th><th>Gate</th></tr></thead>\n"
     "      <tbody>\n"
     (str/join "\n" action-gate-rows) "\n"
     "      </tbody>\n"
     "    </table>\n"
     "  </section>\n"
     "  <section class=\"card\">\n"
     "    <h2>Audit ledger (this run)</h2>\n"
     "    <p class=\"muted\">Append-only decision-fact log — every proposal, hold and commit this scenario produced from the real actor stack.</p>\n"
     "    <table>\n"
     "      <thead><tr><th>Fact</th><th>Op</th><th>Subject</th><th>Basis</th></tr></thead>\n"
     "      <tbody>\n"
     ledger-rows "\n"
     "      </tbody>\n"
     "    </table>\n"
     "  </section>\n"
     "</main>\n"
     "</body></html>\n")))

(defn -main [& args]
  (let [out (or (first args) "docs/samples/operator-console.html")
        result (run-demo!)
        html (render result)
        out-file (java.io.File. out)
        hard-n (count (filter #(= :governor-hold (:t %)) (:ledger result)))]
    (when-let [parent (.getParentFile out-file)]
      (.mkdirs parent))
    (spit out-file html)
    (println "wrote" out "(" (count (:ledger result)) "ledger facts,"
             hard-n "HARD holds )")
    (when (< hard-n 1)
      (binding [*out* *err*]
        (println "ERROR: expected ≥1 HARD hold in demo scenario, got" hard-n))
      (System/exit 1))))
