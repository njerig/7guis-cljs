(ns seven-gooeys.flight-booker
  (:require
    [reagent.core :as r]
    [clojure.string :as string]))

(defn to-date-obj [date-string]
  (let [[day month year] (string/split date-string #"\.")]
    (js/Date. (string/join "-" [year month day]))))

(defn not-a-date? [some-string]
  (js/isNaN (to-date-obj some-string)))

(defn date-input [flight date-type]
  [:input {:type :text
           :value (date-type @flight)
           :on-change #(swap! flight assoc date-type (.. % -target -value))
           :style {:background-color (when (not-a-date? (date-type @flight)) 
                                       "red")}
           :disabled (when (and (= date-type :return) 
                                (= (:type @flight) "one-way")) 
                       true)}])

(defn component []
  (let [flight-state (r/atom {:type "one-way"
                              :depart "30.01.2021"
                              :return "30.01.2021"})]
    (fn []
      [:div.flight-booker-section
        [:h2 "Flight booker"]
        [:form {:on-submit (fn [e]
                             (.preventDefault e)
                             (js/alert (str "You have booked a "
                                            (:type @flight-state)
                                             " flight departing on "
                                            (:depart @flight-state)
                                            (when (= (:type @flight-state) "return")
                                            (str " and returning on " (:return @flight-state)))
                                            ".")))}
          [:select {:value (:type @flight-state)
                    :on-change #(swap! flight-state assoc :type (.. % -target -value))}
            [:option {:value "one-way"} "one-way flight"]
            [:option {:value "return"} "return flight"]]
          [date-input flight-state :depart]
          [date-input flight-state :return]
          [:input {:type :submit
                   :disabled (when (or (and (= (:type @flight-state) "return")
                                            (< (to-date-obj (:return @flight-state)) (to-date-obj (:depart @flight-state))))
                                       (or (not-a-date? (:depart @flight-state)) (not-a-date? (:return @flight-state))))
                               true)}]]])))