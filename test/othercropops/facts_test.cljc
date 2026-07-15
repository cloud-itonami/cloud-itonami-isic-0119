(ns othercropops.facts-test
  (:require [clojure.test :refer [deftest is are testing]]
            [othercropops.facts :as facts]))

(deftest supply-category-lookup
  (testing "Lookup valid supply category"
    (let [c (facts/supply-category-by-id "seed")]
      (is (= "seed" (:id c)))
      (is (= "種苗" (:name c)))))

  (testing "Lookup invalid supply category"
    (is (nil? (facts/supply-category-by-id "unknown")))))

(deftest supply-category-cost-thresholds
  (testing "Category-specific cost thresholds"
    (are [id expected] (= expected (:cost-threshold (facts/supply-category-by-id id)))
      "seed"        500
      "fertilizer"  500
      "equipment"   1000)))

(deftest default-cost-threshold-value
  (testing "Default fallback threshold matches the conservative baseline"
    (is (= 500 facts/default-cost-threshold))))

(deftest other-non-perennial-crop-lookup
  (testing "Lookup valid other-non-perennial crop"
    (are [id expected-name] (= expected-name (:name (facts/other-non-perennial-crop-by-id id)))
      "fodder-corn"      "飼料用トウモロコシ"
      "alfalfa"          "アルファルファ"
      "clover"           "クローバー"
      "forage-kale"      "飼料用ケール"
      "cut-flower"       "切り花"
      "ornamental-plant" "観賞用植物"
      "flower-seed"      "花の種"
      "vegetable-seed"   "野菜の種(ビート種子を除く)"))

  (testing "Lookup invalid crop"
    (is (nil? (facts/other-non-perennial-crop-by-id "unknown"))))

  (testing "Cereals are out of scope (ISIC 0111, not this actor)"
    (is (nil? (facts/other-non-perennial-crop-by-id "wheat"))))

  (testing "Rice is out of scope (ISIC 0112, not this actor)"
    (is (nil? (facts/other-non-perennial-crop-by-id "rice"))))

  (testing "Sugar cane/beet is out of scope (ISIC 0114, not this actor)"
    (is (nil? (facts/other-non-perennial-crop-by-id "sugar-cane"))))

  (testing "Tobacco is out of scope (ISIC 0115, not this actor)"
    (is (nil? (facts/other-non-perennial-crop-by-id "tobacco"))))

  (testing "Fibre crops are out of scope (ISIC 0116, not this actor)"
    (is (nil? (facts/other-non-perennial-crop-by-id "cotton"))))

  (testing "Grapes are out of scope (ISIC 0121, not this actor)"
    (is (nil? (facts/other-non-perennial-crop-by-id "grape")))))
