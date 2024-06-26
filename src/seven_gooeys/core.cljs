(ns seven-gooeys.core
    (:require
      [reagent.dom :as d]
      [seven-gooeys.counter :as counter]
      [seven-gooeys.temp-converter :as temp-converter]
      [seven-gooeys.flight-booker :as flight-booker]
      [seven-gooeys.timer :as timer]
      [seven-gooeys.crud :as crud]
      [seven-gooeys.circle-drawer :as circle-drawer]))


(defn home-page []
  [:div
    [:h1 "7GUIs"]
    [:div.gooeys
      [counter/component]
      [temp-converter/component]
      [flight-booker/component]
      [timer/component]
      [crud/component]
      [circle-drawer/component]]])

;; -------------------------
;; Initialize app

(defn mount-root []
  (d/render [home-page] (.getElementById js/document "app")))

(defn init! []
  (mount-root))
