(ns seven-gooeys.core
    (:require
      [reagent.core :as r]
      [reagent.dom :as d]))

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
  (fn []
    [:div.temperature-converter-section
      [:h2 "Temperature converter"]
      [temperature-input :celsius :fahrenheit]
      [:label "° C"]
      " = "
      [temperature-input :fahrenheit :celsius]
      [:label "° F"]]))

;; 3. Flight booker
(defn flight-booker-component []
  [:div.flight-booker-section
    [:h2 "Flight booker"]])

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
