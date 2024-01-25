(ns moody.helpers.db-helpers)

(defn dissoc-errors
  [db field-id]
  (if field-id
    (update-in db [:errors] dissoc field-id)
    (assoc db :errors nil)))

(defn assoc-error
  [db field-id err-msg]
  (assoc-in db [:errors field-id] err-msg))

(defn assoc-general-error
  [db err-msg]
  (assoc-in db [:errors :general] err-msg))

(defn assoc-errors
  [db errors]
  (assoc db :errors errors))
