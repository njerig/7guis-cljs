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
(defn is-a-float-string [string]
  (not (js/isNaN (js/parseFloat string))))

(def temperature (r/atom {:celsius "" :fahrenheit ""}))

(defn convert [{:keys [celsius fahrenheit] :as temp}]
  (if (nil? fahrenheit)
    (assoc temp :fahrenheit (str (+ 32 (* 1.8 (js/parseFloat celsius)))))
    (assoc temp :celsius (str (* (/ 5 9) (- (js/parseFloat fahrenheit) 32))))))

(defn temperature-input [scale invalidates]
  [:input {:type :text
           :value (scale @temperature)
           :on-change (fn [e]
                        (let [new-value (.. e -target -value)]
                          (if (is-a-float-string new-value)
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
  [:div.temperature-converter-section
    [:h2 "Temperature converter"]
    [temperature-input :celsius :fahrenheit]
    [:label "° C"]
    " = "
    [temperature-input :fahrenheit :celsius]
    [:label "° F"]])

;; 3. Flight booker
(def flight-state (r/atom {:type "one-way"
                           :depart "30.01.2021"
                           :return "30.01.2021"}))

(defn to-date-obj [date-string]
  (let [[day month year] (string/split date-string #"\.")]
    (js/Date. (string/join "-" [year month day]))))

(defn is-not-a-date [some-string]
  (js/isNaN (to-date-obj some-string)))

(defn date-input [date-type]
  [:input {:type :text
           :value (date-type @flight-state)
           :on-change #(swap! flight-state assoc date-type (.. % -target -value))
           :style {:background-color (when (is-not-a-date (date-type @flight-state)) 
                                       "red")}
           :disabled (when (and (= date-type :return) 
                                (= (:type @flight-state) "one-way")) 
                       true)}])

(defn flight-booker-component []
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
                :on-change (fn [e]
                             (swap! flight-state assoc :type (.. e -target -value)))}
        [:option {:value "one-way"} "one-way flight"]
        [:option {:value "return"} "return flight"]]
      [date-input :depart]
      [date-input :return]
      [:input {:type :submit
               :disabled (when (or (and (= (:type @flight-state) "return")
                                        (< (to-date-obj (:return @flight-state)) (to-date-obj (:depart @flight-state))))
                                   (or (is-not-a-date (:depart @flight-state)) (is-not-a-date (:return @flight-state))))
                           true)}]]])

(defn timer-component []
  [:div.timer-section
    [:h2 "Timer"]])

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
