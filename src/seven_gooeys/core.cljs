(ns seven-gooeys.core
    (:require
      [reagent.core :as r]
      [reagent.dom :as d]
      [clojure.string :as string]))

;; -------------------------
;; Views

;; 1. Counter
(defn counter-component []
  (let [counter (r/atom 0)]
    (fn []
      [:div.counter-section
        [:h2 "Counter"]
        [:input {:type "text" :read-only true :value @counter}]
        [:input {:style {:margin-left "10px"}
                 :type "button"
                 :value "+ 1"
                 :on-click #(swap! counter inc)}]])))

;; 2. Temperature converter
(defn float-string? [string]
  (not (js/isNaN (js/parseFloat string))))

(defn convert [{:keys [celsius fahrenheit] :as temp}]
  (if (nil? fahrenheit)
    (assoc temp :fahrenheit (str (+ 32 (* 1.8 (js/parseFloat celsius)))))
    (assoc temp :celsius (str (* (/ 5 9) (- (js/parseFloat fahrenheit) 32))))))

(defn temperature-input [temperature scale invalidates]
  [:input {:type :text
           :value (scale @temperature)
           :on-change (fn [e]
                        (let [new-value (.. e -target -value)]
                          (if (float-string? new-value)
                            (swap! temperature
                                   #(-> %
                                        (assoc scale new-value)
                                        (dissoc invalidates)
                                        convert))
                            (swap! temperature
                                   #(-> %
                                       (assoc scale new-value)
                                       (assoc invalidates ""))))))}])

(defn temperature-converter-component []
  (let [temperature (r/atom {:celsius "" :fahrenheit ""})]
    (fn []
      [:div.temperature-converter-section
        [:h2 "Temperature converter"]
        [temperature-input temperature :celsius :fahrenheit]
        [:label "° C"]
        " = "
        [temperature-input temperature :fahrenheit :celsius]
        [:label "° F"]])))

;; 3. Flight booker
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

(defn flight-booker-component []
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

(defn timer-component []
  [:div.timer-section
    [:h2 "Timer"]
    [:div
      [:div
        [:span {:style {:min-width "2rem" :margin-right "5px"}} "Elapsed Time:"]
        [:meter {:style {:flex-grow 1}}]]
      [:div
       [:label "10s"]]
      [:div {:style {:display "flex"}}
       [:div {:style {:min-width "2rem"}} "Duration:"]
       [:div [:input {:type :range
                      :style {:flex-grow 1}}]]]]])

(defn crud-component []
  [:div.crud-section
    [:h2 "CRUD"]])

(defn circle-drawer-component []
  [:div.circle-drawer-section
    [:h2 "Circle drawer"]])

(defn cells-component []
  [:div.cells-section
    [:h2 "Cells"]])


(defn home-page []
  [:div
    [:h1 "7GUIs"]
    [:div.gooeys
      [counter-component]
      [temperature-converter-component]
      [flight-booker-component]
      [timer-component]
      [crud-component]
      [circle-drawer-component]
      [cells-component]]])

;; -------------------------
;; Initialize app

(defn mount-root []
  (d/render [home-page] (.getElementById js/document "app")))

(defn init! []
  (mount-root))
