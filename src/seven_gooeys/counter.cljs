(ns seven-gooeys.counter
  (:require
    [reagent.core :as r]))

(defn component []
  (let [counter (r/atom 0)]
    (fn []
      [:div.counter-section
        [:h2 "Counter"]
        [:input {:type "text" :read-only true :value @counter}]
        [:input {:style {:margin-left "10px"}
                 :type "button"
                 :value "+ 1"
                 :on-click #(swap! counter inc)}]])))