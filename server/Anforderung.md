Als Kunde m�chte ich Bestellungen auch noch stornieren k�nnen, wenn sie bereits bezahlt wurden. 
--> Stolperfalle (weil nicht in Storybeschreibung gekl�rt): Kann ich Bestellungen noch stornieren, wenn ich den Beleg abgeholt habe? (fachliche Antwort: Nein)
--> Fachliche Frage (wird nicht ber�cksichtigt) Wie geschieht die R�ckbuchung der Bezahlung? 

Als Kunde m�chte ich nach der Bestellung nach meiner RestBucks Kundenkarte befragt werden und diese einscannen lassen, damit ich die RestBucks Bonuspunkte sammeln kann. 
-> Die Kundenkarte muss im Bestellprozess erfasst werden k�nnen
---> Neuer Link: "customercard" auf order resource
---> Neue Resource ala Payment f�r die gescannte Karte
-> Ein anderer BC (Bonussystem) muss �ber die Bestellung informiert werden
---> Wie teilen wir dem Bonussystem BC die Kartennummer mit?

Aufkommende Fragen: 
-------------------
- Geh�rt das in den PaymentController oder sollten wir einen neuen Controler (CutomerCardController) schreiben? Geh�rt VERMUTLICH zum Payment BC
- Ist das Repo �berhaupt notwendig (commit 91826aa89c52c03227325afe881feac29a0ff9c6)? Reicht nicht die Web Schicht? 

Sch�ne Story an der man sieht, was und wie an der REST-Schnittstelle getestet werden kann/soll: 
Die Bestell�bersicht soll nicht mehr aufrufbar sein (es soll ein forbidden geliefert werden). 
Erweiterte Schwierigkeiten, da Einf�hren von Parametern: Die Bestell�bersicht soll nur noch f�r Kunden mit Kundenkarte aufrufbar sein. 
