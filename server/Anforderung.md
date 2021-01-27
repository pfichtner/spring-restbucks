Als Kunde möchte ich Bestellungen auch noch stornieren können, wenn sie bereits bezahlt wurden. 
--> Stolperfalle (weil nicht in Storybeschreibung geklärt): Kann ich Bestellungen noch stornieren, wenn ich den Beleg abgeholt habe? (fachliche Antwort: Nein)
--> Fachliche Frage (wird nicht berücksichtigt) Wie geschieht die Rückbuchung der Bezahlung? 

Als Kunde möchte ich nach der Bestellung nach meiner RestBucks Kundenkarte befragt werden und diese einscannen lassen, damit ich die RestBucks Bonuspunkte sammeln kann. 
-> Die Kundenkarte muss im Bestellprozess erfasst werden können
---> Neuer Link: "customercard" auf order resource
---> Neue Resource ala Payment für die gescannte Karte
-> Ein anderer BC (Bonussystem) muss über die bezahlte Bestellung informiert werden
-> Das Bonussystem muss an die Kundenkarten-Nummer sowie den Betrag kommen (REST)

Aufkommende Fragen: 
-------------------
- Gehört das in den PaymentController oder sollten wir einen neuen Controler (CutomerCardController) schreiben? Gehört VERMUTLICH zum Payment BC ---> Falsch!
- Ist das Repo überhaupt notwendig (commit 91826aa89c52c03227325afe881feac29a0ff9c6)? Reicht nicht die Web Schicht? ---> Nein, weil wir die Scans persistieren müssen

Schöne Story an der man sieht, was und wie an der REST-Schnittstelle getestet werden kann/soll: 
Die Bestellübersicht soll nicht mehr aufrufbar sein (es soll ein forbidden geliefert werden). Die Link-Relation soll natürlich auch nicht mehr auf Root ausgeliefert werden. 
Erweiterte Schwierigkeiten, da Einführen von Parametern: Die Bestellübersicht soll nur noch für Kunden mit Kundenkarte aufrufbar sein. 
