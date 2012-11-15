(ns deidara.core
  (use deidara.net clojure.string))

(def irc-port 6667)

(defn send-command [bot command]
  (do
    (print (format "Sending %s\n" command))
    (write-to (:output bot) (format "%s\r\n",command))))

(defn process [message callback]
  (callback message))

(defn process-messages [message-text callback]
  (let [lastcrlf (last-index-in message-text "\r\n")
	split-messages (split (subs message-text 0 lastcrlf) #"\r\n")]
	(do
	  (doseq [message split-messages] (process message callback))
	  (subs message-text lastcrlf)))) 

(defn on-message-from [bot callback]
  (defn wait-and-receive-messages [previous]
    (let [buf (byte-array 512)
	  bytes-read (read-from (:input bot) buf)
	  current (str previous (String. buf "ascii"))]
      (if (> bytes-read 0)
	(recur (process-messages current callback)))))
  (wait-and-receive-messages ""))
		 

(defn bot [nick server channel]
  (let [socket (socket server irc-port)
	input (input-stream socket)
	output (output-stream socket)
	created-bot {:input input :output output}]
    (do
      (.println *err* "Connected")
      (send-command created-bot (format "USER guest localhost localhost :Deidara"))
      (send-command created-bot (format "NICK %s" nick))
      (send-command created-bot (format "JOIN %s" channel))
      created-bot)))


(def bot (bot "yellowflash" "irc.freenode.net" "#testingmyirc"))

(defn print-it [msg]
  (print (str msg "\n")))
(print "Starting the event loop")
(on-message-from bot print-it)