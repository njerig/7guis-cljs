(ns seven-gooeys.crud
  (:require
    [reagent.core :as r]
    [clojure.string :as string]))

(defn name-field [name-type new-name]
  [:input {:type :text
           :value (name-type @new-name)
           :on-change #(swap! new-name assoc name-type (.. % -target -value))}])

(defn button [value names selected new-name]
  [:button {:value value
            :on-click (case value
                        "Create" (fn [e]
                                   (swap! names conj (str (:surname @new-name) 
                                                          ", " 
                                                          (:name @new-name)))
                                   (reset! new-name {:name "" :surname ""}))
                        "Update" (fn [e]
                                   (let [idx (.indexOf @names @selected)]
                                     (swap! names assoc idx (str (:surname @new-name)
                                                                 ", "
                                                                 (:name @new-name)))))
                        "Delete" (fn [e]
                                   (reset! names (remove #(= @selected %) @names))))
            :disabled (case value
                        "Create"      (when (or (= (:name @new-name) "")
                                                (= (:surname @new-name) ""))
                                        true)
                        (or "Update"
                            "Delete") (when (= @selected "") true))} value])

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
        selected (r/cursor state [:selected])]
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
            (into [:select {:size 6
                            :id "listbox"
                            :on-change #(reset! selected (.. % -target -value))}]
              (map
                (fn [name] [:option {:key name} name])
                (filter #(true? (string/starts-with? % (string/capitalize @filter-prefix))) 
                        @names)))]
          [:div
            [:div
              [:div {:style {:width 88
                             :display "inline-block"}}
                [:label "Name:"]]
                [name-field :name new-name]]
            [:div
              [:div {:style {:display "inline-block"}}
                [:label "Surname:"]]
                [name-field :surname new-name]]]]
        [:div
          [button "Create" names selected new-name]
          [button "Update" names selected new-name]
          [button "Delete" names selected new-name]]])))