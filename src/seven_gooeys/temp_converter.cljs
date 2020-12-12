(ns seven-gooeys.temp-converter
  (:require
    [reagent.core :as r]))

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

(defn component []
  (let [temperature (r/atom {:celsius "" :fahrenheit ""})]
    (fn []
      [:div.temperature-converter-section
        [:h2 "Temperature converter"]
        [temperature-input temperature :celsius :fahrenheit]
        [:label "° C"]
        " = "
        [temperature-input temperature :fahrenheit :celsius]
        [:label "° F"]])))