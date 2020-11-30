(ns seven-gooeys.core
    (:require
      [reagent.core :as r]
      [reagent.dom :as d]))

;; -------------------------
;; Views

(defn counter-section []
  [:div.counter-section
    [:h2 "Counter"]])

(defn temperature-converter-section []
  [:div.temperature-converter-section
    [:h2 "Temperature converter"]])

(defn flight-booker-section []
  [:div.flight-booker-section
    [:h2 "Flight booker"]])

(defn timer-section []
  [:div.timer-section
    [:h2 "Timer"]])

(defn crud-section []
  [:div.crud-section
    [:h2 "CRUD"]])

(defn circle-drawer-section []
  [:div.circle-drawer-section
    [:h2 "Circle drawer"]])

(defn cells-section []
  [:div.cells-section
    [:h2 "Cells"]])


(defn home-page []
  [:div
    [:h1 "7GUIs"]
    [:div.gooeys
      [counter-section]
      [temperature-converter-section]
      [flight-booker-section]
      [timer-section]
      [crud-section]
      [circle-drawer-section]
      [cells-section]]])

;; -------------------------
;; Initialize app

(defn mount-root []
  (d/render [home-page] (.getElementById js/document "app")))

(defn init! []
  (mount-root))
