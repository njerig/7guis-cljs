(ns seven-gooeys.circle-drawer
  (:require
    [reagent.core :as r]))

(def default-diameter 20)

(defn svg-pos [client-pos svg-elem]
  (let [[x y] client-pos
        svg-pt (.createSVGPoint svg-elem)
        matrix (-> svg-elem .getScreenCTM .inverse)]
    (set! (.-x svg-pt) x)
    (set! (.-y svg-pt) y)
    (let [gpt (.matrixTransform svg-pt matrix)]
      [(int (.-x gpt)) (int (.-y gpt))])))

(defn generate-id [state]
  (swap! state update :id inc)
  (:id @state))

(defn add-action [state action]
  (let [old-history (subvec (:actions @state) 0 (inc (:index @state)))
        new-history (conj old-history action)]
    (swap! state #(-> %
                      (assoc :actions new-history)
                      (update :index inc)))))

(defn draw-circle [state [x y]]
  (swap! state assoc-in [:circles (generate-id state)] {:position [x y]
                                                        :diameter default-diameter}))

(defn add-draw-to-actions [state [x y]]
  (add-action state [:draw-circle (:id @state) {:position [x y]
                                                :diameter default-diameter}]))

(defn edit-circle [state id new-diameter]
  (swap! state assoc-in [:circles id :diameter] new-diameter))

(defn add-edit-to-actions [state id new-diameter]
  (add-action state [:edit-circle id {:diameter new-diameter}]))

(defn deselect-circle [state]
  (swap! state #(assoc-in % [:selected-id] nil)))

(defn find-previous-diameter [state id]
  (let [current-actions (take (:index @state) (:actions @state))
        relevant-actions (filter #(= id (second %)) current-actions)
        relevant-action (last relevant-actions)
        params (last relevant-action)]
    (:diameter params)))

(defn reverse-action [state]
  (let [last-action (nth (:actions @state) (:index @state))
        [action-type id _] last-action]
    (case action-type
      :draw-circle (swap! state update :circles dissoc id)
      :edit-circle (swap! state assoc-in [:circles id :diameter] (find-previous-diameter state id)))))

(defn undo [state]
  (when (and (>= (:index @state) 0)
             (<= (:index @state) (-> @state :actions count dec)))
    (reverse-action state)
    (swap! state update :index dec)))

(defn redo [state]
  (when (and (>= (:index @state) 0) 
             (<= (:index @state) (- (-> @state :actions count) 2)))
    (swap! state update :index inc)
    (let [[action-type id {:keys [diameter position]}] (nth (:actions @state) (:index @state))]
      (case action-type
        :draw-circle (draw-circle state position)
        :edit-circle (edit-circle state id diameter)))))

(defn select-current-actions [state]
  (-> @state :actions (subvec 0 (inc (:index @state)))))

(defn construct-circles-map [actions]
  (reduce (fn [circles [action id param]]
              (case action
                :draw-circle (assoc circles id param)
                :edit-circle (update circles id assoc :diameter param)))
            {}
            actions))

(defn calc-pos [coord popup-dim canvas-dim]
  (let [max-coord (- canvas-dim popup-dim)]
    (if (> coord max-coord)
      (- coord popup-dim)
      coord)))

(defn calc-popup-pos [[cx cy] [popup-width popup-height] canvas]
  (let [canvas-width (.-clientWidth @canvas)
        canvas-height (.-clientHeight @canvas)]
    [(calc-pos cx popup-width canvas-width)
     (calc-pos cy popup-height canvas-height)]))

(defn adjust-dialog [circle canvas show-state state]
  (let [id (:selected-id @state)
        cx (get-in @circle [:position 0])
        cy (get-in @circle [:position 1])
        dimensions [148 66]
        dialog-pos (calc-popup-pos [cx cy] dimensions canvas)]
    (fn []
      [:foreignObject {:x (dialog-pos 0)
                       :y (dialog-pos 1)
                       :width (dimensions 0)
                       :height (dimensions 1)
                       :style (if (:show-adjust @show-state)
                                {:position :absolute}
                                {:display "none"})
                       :className "popup"}
        [:div {:id "adjust"
               :on-click (fn [e]
                           (.preventDefault e)
                           (.stopPropagation e))}
          [:button {:on-click (fn [e]
                                (.stopPropagation e)
                                (deselect-circle state))} 
           "✕"]
          [:div {:style {:margin-left 5}}
            [:label "Adjust diameter"]
            [:div {:style {:width 104}}
              [:input {:style {:width "100%" :margin 0}
                       :type :range
                       :min 1
                       :max 100
                       :value (:diameter @circle)
                       :on-click #(.stopPropagation %)
                       :on-change (fn [e]
                                    (let [new-value (-> e .-target .-value int)]
                                      (edit-circle state id new-value)))
                       :on-mouse-up (fn [_]
                                      (add-edit-to-actions state id (:diameter @circle)))}]]]]])))

(defn context-menu [circle canvas show-state]
  (let [cx (get-in @circle [:position 0])
        cy (get-in @circle [:position 1])
        dimensions [105 30]
        menu-pos (calc-popup-pos [cx cy] dimensions canvas)]
    (fn []
      [:foreignObject {:x (menu-pos 0)
                       :y (menu-pos 1)
                       :width (dimensions 0)
                       :height (dimensions 1)
                       :style (if (:show-menu @show-state)
                                {:position :absolute}
                                {:display "none"})
                       :className "popup"}
        [:div {:id "menu"}
          [:ul
            [:li {:on-click (fn [e]
                              (.preventDefault e)
                              (.stopPropagation e)
                              (swap! show-state #(-> %
                                                (assoc :show-menu false)
                                                (assoc :show-adjust true))))} 
             "Adjust diameter..."]]]])))

(defn selected-popup [state canvas]
  (let [show-state (r/atom {:show-menu   true
                            :show-adjust false})
        id (:selected-id @state)
        circle (r/cursor state [:circles id])]
    (fn []
      [:<>
        [context-menu circle canvas show-state]
        [adjust-dialog circle canvas show-state state]])))

(defn circle-component [state id params]
  [:circle {:cx (get-in params [:position 0])
            :cy (get-in params [:position 1])
            :r  (/ (:diameter params) 2)
            :fill "white"
            :stroke "black"
            :stroke-width 1
            :on-click #(.stopPropagation %)
            :on-context-menu #(do (.stopPropagation %)
                                  (.preventDefault %)
                                  (swap! state assoc :selected-id id))}])

(defn component []
  (let [state (r/atom {:actions []
                       :index -1
                       :id 0
                       :selected-id nil
                       :circles {}})
        index (r/cursor state [:index])
        canvas (atom nil)]
    (fn []
      [:div.circle-drawer-section
        [:h2 "Circle drawer"]
        [:div
          [:div {:style {:display "flex"
                         :justify-content "center"
                         :margin-bottom "1em"}}
            [:button {:value "undo"
                      :on-click #(undo state)
                      :disabled (when (< @index 0) true)} "Undo"]
            [:button {:value "redo"
                      :on-click #(redo state)
                      :disabled (when (= @index (-> @state :actions count dec)) true)} "Redo"]]
            [:svg#canvas {:xmlns "http://www.w3.org/2000/svg"
                          :width "100%"
                          :ref (fn [el]
                                 (reset! canvas el)) 
                          :style {:min-height "200px"
                                  :background-color "#ffffff"
                                  :border "1px solid black"}
                                  :on-click (fn [e]
                                              (deselect-circle state)
                                              (let [mouse-pos
                                                    (svg-pos [(.-clientX e)
                                                              (.-clientY e)]
                                                             (.-target e))]
                                                (draw-circle state mouse-pos)
                                                (add-draw-to-actions state mouse-pos)
                                                (js/console.log "index: " @index)))}
              (map
                (fn [[circle-id circle-param]]
                  ^{:key (str "circle_" circle-id)} [circle-component state
                                                                      circle-id
                                                                      circle-param])
                (@state :circles))
                (when (some? (:selected-id @state))
                  [selected-popup state canvas])]]])))