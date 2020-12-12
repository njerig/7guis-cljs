(ns seven-gooeys.crud
  (:require
    [reagent.core :as r]
    [clojure.string :as string]))

(defn component []
  (let [state (r/atom {:filter-prefix ""
                       :names ["Emil, Hans"
                               "Mustermann, Max"
                               "Tisch, Roman"]
                       :new-name {:name ""
                                  :surname ""}
                       :selected ""})
        filter-prefix (r/cursor state [:filter-prefix])
        names (r/cursor state [:names])
        new-name (r/cursor state [:new-name])
        selected (r/cursor state [:selected])
        new-name-string (str (:surname @new-name) ", " (:name @new-name))]
    (fn []
      [:div.crud-section
        [:h2 "CRUD"]
        [:div
          [:div
            [:label "Filter prefix:"]
            [:input {:type :text
                     :value @filter-prefix
                     :on-change #(reset! filter-prefix (.. % -target -value))}]]]
        [:div
          [:div {:style {:flex-grow 1
                         :margin-right "20px"}}
            [:select {:size 6
                      :id "listbox"
                      :value @selected
                      :on-change #(reset! selected (.. % -target -value))}
              (map
                (fn [name] [:option {:key name} name])
                (filter #(true? (string/starts-with? % (string/capitalize @filter-prefix))) @names))]]
          [:div
            [:div
              [:div {:style {:width 88
                             :display "inline-block"}}
                [:label "Name:"]]
                [:input {:type :text
                         :value (:name @new-name)
                         :on-change #(swap! new-name assoc :name (.. % -target -value))}]]
            [:div
              [:div {:style {:display "inline-block"}}
                [:label "Surname:"]]
                [:input {:type :text
                         :value (:surname @new-name)
                         :on-change #(swap! new-name assoc :surname (.. % -target -value))}]]]]
        [:div
          [:button {:value "Create"
                    :on-click (fn [e]
                               (swap! names conj new-name-string)
                               (reset! new-name {:name "" :surname ""}))
                    :disabled (when (or (= (:name @new-name) "")
                                       (= (:surname @new-name) ""))
                               true)} "Create"]
          [:button {:value "Update"
                    :on-click (fn [e]
                               (let [idx (.indexOf @names @selected)]
                                 (swap! names assoc idx new-name-string)))
                   :disabled (when (= @selected "") true)} "Update"]
          [:button {:value "Delete"
                    :on-click (fn [e]
                               (reset! names (remove #(= @selected %) @names)))
                    :disabled (when (= @selected "") true)} "Delete"]]])))