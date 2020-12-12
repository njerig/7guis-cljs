(ns seven-gooeys.core
    (:require
      [reagent.core :as r]
      [reagent.dom :as d]
      [clojure.string :as string]
      [seven-gooeys.counter :as counter]
      [seven-gooeys.temp-converter :as temp-converter]
      [seven-gooeys.flight-booker :as flight-booker]
      [seven-gooeys.timer :as timer]
      [seven-gooeys.crud :as crud]))

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
      [counter/component]
      [temp-converter/component]
      [flight-booker/component]
      [timer/component]
      [crud/component]
      [circle-drawer-component]
      [cells-component]]])

;; -------------------------
;; Initialize app

(defn mount-root []
  (d/render [home-page] (.getElementById js/document "app")))

(defn init! []
  (mount-root))
