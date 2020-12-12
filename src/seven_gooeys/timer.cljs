(ns seven-gooeys.timer
  (:require
    [reagent.core :as r]))

(defn component []
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
                         :defaultValue @duration
                         :on-input #(reset! duration 
                                            (.. % -target -value))}]]
              [:div {:style {:margin-top 20}}
                [:input {:type :reset
                         :on-click #(reset! elapsed 0)}]]]])})))