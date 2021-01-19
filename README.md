# Let's FindUs by ArceTeam 
**Membri gruppo:**
* Casarotti Giulio 876589
* Ferrari Simone 875921
* Gallo Giulia 874214
* Trolese Giulio 875926

## Note sul progetto
### Breve descrizione
Il progetto proposto consiste in un'applicazione per dispositivi mobili Android che permetta di mantenere uno storico di 7 giorni di tutte le persone che si sono incrociate lungo la strada e nei luoghi frequentati. 

In particolare, la nostra applicazione dovrebbe essere in grado di scambiare informazioni con i dispositivi che entrano nel raggio d’azione del Bluetooth Low Energy del nostro smartphone. 

### Problemi noti:
Durante lo sviluppo dell'applicazione, abbiamo riscontrato delle difficoltà implementative.  
Di seguito, sono riportati gli attuali problemi a noi noti.   
Vengono fornite, inoltre, possibili risoluzioni che, per motivi di tempo, non sono state sviluppate.  

* **Ricerca di un luogo sulla mappa:** Sono stati riscontrati dei problemi con le API Places di Google. Questi sono dovuti dall'impossibilità di abilitare il "billing" delle API, pertanto Google non ci fornisce correttamente il servizio.  
*SOLUZIONE:* Abilitare il billing inserendo le coordinate bancarie di un conto corrente.  

* **Bluetooth in background non funziona:** La ricerca dei dispositivi svolta in background richiede intervalli di tempo troppo elevati (tra una scansione e l'altra) che, per i nostri scopi, è inappropriato.  
*SOLUZIONE:* Per il momento, non è stata identificata alcuna soluzione. Sarà oggetto di studi futuri.  

* **Tempistiche di trasferimento dati elevate:** Il trasferimento dell'immagine mediante BLE è risultata più costosa, in termini di tempo, del previsto. La limitata capacità di trasferimento del BLE ne è la principale causa.  
*SOLUZIONE 1:* Trovare un modo efficiente per inviare l'immagine mediante il Bluetooth normale.  
*SOLUZIONE 2:* Introdurre un server centrale, con annesso database, avente ruolo di gestire i profili degli utenti. In questo modello, nella fase d'incontro, i dispositivi non scambierebbero più molti dati, ma semplicemente un codice identificativo per poter poi recuperare le informazioni dal server.   

* **Il dispositivo master gestisce una sola connessione alla volta:** Allo stato delle cose, siamo in grado di scambiare i rispettivi profili (dati e immagine) solo tra due dispositivi per volta. Il problema sorge date le elevate tempistiche richieste per l'invio dei dati.  
*SOLUZIONE:* Se la *SOLUZIONE 2* per il problema precedente verrà implementata, automaticamente sarà risolto anche questo.  

* **Bug minori:** Sono presenti alcuni bug di minor importanza, principalmente relativi alla UI.  
*SOLUZIONE:* In futuro verranno analizzati nel dettaglio tali problemi e studiati caso per caso.   
