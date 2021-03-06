(ns ^:figwheel-hooks app.build.dev
  (:require [app.core      :as core]
            [re-frisk.core :as frisk]))

(frisk/enable-re-frisk! {:width "400px" :height "500px"})

(defn ^:after-load render [] (core/mount))

(core/mount)
