(ns seven-gooeys.prod
  (:require
    [seven-gooeys.core :as core]))

;;ignore println statements in prod
(set! *print-fn* (fn [& _]))

(core/init!)
