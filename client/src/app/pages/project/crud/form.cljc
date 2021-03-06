(ns app.pages.project.crud.form
  (:require [re-frame.core :as rf]
            [app.helpers :as h]
            [app.form.events :as ze]
            [zframes.mapper :as zm]
            [zframes.flash   :as flash]
            [zenform.model :as zf]))

(def path [:form ::form])
(def schema
  {:type   :form
   :fields {:name        {:type :string
                          :validators {:required {:message "Укажите название"}}
                          }
            :id          {:type :string}
            :description {:type :string}
            :author      {:type      :object
                          :display-paths [[:display]]
                          :on-search ::ze/search-author}
            :test {:type :string
                   :items [{:display "lol" :value "kek"}]}
            :payment     {:type   :form
                          :fields {:regional  {:type :string}
                                   :municipal {:type :string}
                                   :federal   {:type :string}
                                   :other     {:type :string}}}
            :img         {:type :string}
            :project     {:type :string}
            :category    {:type :string}
            :period      {:type :form
                          :fields {:start {:type :string}
                                   :end   {:type :string}}}}})

(def author-path [:form ::author])
(def author
  {:type   :form
   :fields {:name      {:type :string}
            :surname   {:type :string}
            :last-name {:type :string}
            :position  {:type :string}
            :photo     {:type :string}}})

(def au-mapper
  [[[:name] [:name 0 :given 0]]
   [[:position] [:position]]
   [[:photo] [:photo]]
   [[:last-name] [:name 0 :given 1]]
   [[:surname] [:name 0 :family]]])

(rf/reg-event-fx
 ::set-a-img
 (fn [_ [_ _ file-meta]]
   {:dispatch-n [[::flash/remove-flash :file-load]
                 [:zf/set-value author-path [:photo] (:url file-meta)]]}))

(rf/reg-event-fx
 ::set-img
 (fn [_ [_ _ file-meta]]
   {:dispatch-n [[:zf/set-value path [:img] (:url file-meta)]
                 [:zframes.flash/remove-flash :file-load]]}))

(rf/reg-event-fx
 ::init
 (fn [coeff [_ {:keys [data]}]]
   {:dispatch-n [[:zf/init path schema data]]}))

(defn eval-form [db cb]
  (let [{:keys [errors value form]} (-> db (get-in path) zf/eval-form)]
    (merge
     {:db (assoc-in db path form)}
     (if (empty? errors)
       (cb (cond-> value
             (:project value)
             (update-in [:project :id] h/parseInt)
             (:author value)
             (update-in [:author :id] h/parseInt)))
       #?(:clj  (println errors)
          :cljs (.warn js/console "Form errors: " (clj->js errors)))))))

(defn eval-author [db cb]
  (let [{:keys [errors value form]} (-> db (get-in author-path) zf/eval-form )]
    (merge
     {:db (assoc-in db author-path form)}
     (if (empty? errors)
       (cb (zm/export value au-mapper))
       #?(:clj  (println errors)
          :cljs (.warn js/console "Form errors: " (clj->js errors)))))))
