(ns moody.errors.errors-events
  (:require
   [day8.re-frame.tracing-stubs :refer [fn-traced]]
   [moody.helpers.db-helpers :refer [dissoc-errors]]
   [re-frame.core :refer [reg-event-db]]))

(def errors-interceptors [#_check-spec-interceptor])

(reg-event-db
 :dissoc-errors
 errors-interceptors
 (fn-traced [db [_ field-id]]
            (dissoc-errors db field-id)))
