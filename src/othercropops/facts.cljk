(ns othercropops.facts
  "Reference facts for other-non-perennial-crop growing operations
  coordination: supply category cost policy and crop classification. This
  namespace contains pure lookup functions for domain reference data -- the
  Governor and Advisor consult these instead of inventing thresholds.
  Mirrors `vegops.facts` (cloud-itonami-isic-0113) in shape.")

(def supply-categories
  "Procurement categories this actor may propose orders for, and the
  default cost threshold above which an order proposal must escalate for
  human sign-off (farmer/ops-manager)."
  {"seed"
   {:id "seed" :name "種苗" :cost-threshold 500}

   "fertilizer"
   {:id "fertilizer" :name "肥料" :cost-threshold 500}

   "equipment"
   {:id "equipment" :name "設備" :cost-threshold 1000}})

(defn supply-category-by-id [id]
  (get supply-categories id))

(def default-cost-threshold
  "Fallback escalation threshold used when a supply-order proposal doesn't
  cite a known category (never invent a lower bar than this)."
  500)

(def other-non-perennial-crops
  "Other non-perennial crops n.e.c. this actor's field records may cover
  (ISIC 0119: growing of other non-perennial crops -- fodder/forage crops,
  flower and vegetable seed crops, cut flowers and ornamental plants for
  cutting, and other annual crops not elsewhere classified. Cereals are
  ISIC 0111, rice is ISIC 0112, sugar cane/beet is ISIC 0114, tobacco is
  ISIC 0115, fibre crops are ISIC 0116, and grapes are ISIC 0121, all out
  of scope)."
  {"fodder-corn"      {:id "fodder-corn" :name "飼料用トウモロコシ"}
   "alfalfa"          {:id "alfalfa" :name "アルファルファ"}
   "clover"           {:id "clover" :name "クローバー"}
   "forage-kale"      {:id "forage-kale" :name "飼料用ケール"}
   "cut-flower"       {:id "cut-flower" :name "切り花"}
   "ornamental-plant" {:id "ornamental-plant" :name "観賞用植物"}
   "flower-seed"      {:id "flower-seed" :name "花の種"}
   "vegetable-seed"   {:id "vegetable-seed" :name "野菜の種(ビート種子を除く)"}})

(defn other-non-perennial-crop-by-id [id]
  (get other-non-perennial-crops id))
