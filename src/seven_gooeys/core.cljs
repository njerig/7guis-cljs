(ns seven-gooeys.core
    (:require
      [reagent.core :as r]
      [reagent.dom :as d]
      [clojure.string :as string]
      [goog.string :as gstring]
      [goog.string.format]))

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

;; 4. Timer
(defn timer-component []
  (let [timer    (r/atom {:elapsed 0 :duration 30})
        elapsed  (r/cursor timer [:elapsed])
        duration (r/cursor timer [:duration])]
    (r/create-class 
      {:component-did-mount
        #(def tid (js/setInterval
                    (fn []
                      (when (< @elapsed @duration)
                        (swap! elapsed inc)))
                    1000))
       :component-will-unmount
        (js/clearInterval tid)
       :reagent-render 
        (fn []
          [:div.timer-section
            [:h2 "Timer"]
            [:div
              [:div
                [:div [:span "Elapsed time:"]]
                [:meter {:min 0
                         :max @duration
                         :value @elapsed}]]
              [:div
                [:div]
                [:label (str @elapsed "s")]]
              [:div
                [:div [:span "Duration:"]]
                [:input {:type :range
                         :min 0
                         :max 60
                         :step 1
                         :value @duration
                         :on-input #(reset! duration 
                                            (.. % -target -value))}]]
              [:div {:style {:margin-top 20}}
                [:input {:type :reset
                         :on-click #(reset! elapsed 0)}]]]])})))

;; 5. CRUD
(defn crud-component []
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
