# Sudoku-Game

Progetto per l'esame di Architetture Distribuite per il Cloud dell'anno di corso 2021/2022. Laurea magistrale in Computer Science, curriculum in Cloud Computing.

## Introduzione

L'obiettivo del progetto è quello di implementare un gioco multiplayer basato sul Sudoku. 
Gli utenti, dopo aver effettuato l'accesso inserendo un nickname di lunghezza
compresa tra 3 e 7 lettere, accedono al tabellone sfide in cui possono generare una nuova partita
inserendo "@" insieme al codice partita oppure unirsi ad una delle partite disponibili presenti nel tabellone.

Alla creazione di una sfida, verrà presentato un sudoku da completare, la lista comandi e la lista
dei partecipanti con rispettivo punteggio. La partita inizierà non appena un secondo giocatore avrà effettuato
l'accesso. Una volta avviata la partita, ogni giocatore può abbandonare la partita o inserire un valore all'interno 
della board del sudoku. Per inserire un valore N nella cella (x,y) bisognerà digitare "xy-N". Se il valore inserito
è corretto e non presente fino a quel momento, il giocatore guadagnerà un punto, se già presente non guadagnerà punti,
se il valore è errato perderà un punto. Una volta completato il sudoku, verrà mostrato a video il vincitore con il rispettivo punteggio
e un countdown di secondi prima che ogni giocatore venga reindirizzato al tabellone sfide. La partita terminerà se il tabellone è completato
oppure il numero di giocatori rimasti sono pari a 1, in questo caso non ci sarà un vincitore e il giocatore verrà reindirizzato al tabellone sfide.

Un giocatore presente nel tabellone sfide può partecipare ad una delle partite disponibili mostrate a video inserendo 
">" insieme al codice partita. Una volta entrato a far parte di una partita non può partecipare ad altre sfide. Una volta abbandonata 
la sfida, può accedere ad altre sfide o crearne nuove. 

## Architettura



## Strumenti utilizzati


Il progetto è un applicativo Java sviluppato con TomP2P, libreria utilizzata per creare connessioni Peer-to-Peer e creare una rete DHT. 

Per la parte grafica è stata utilizzata Beryx, una libreria per creare console interattive tramite terminale.

Il progetto, inoltre, è stato testato tramite l'uso JUnit 5.

Infine l'intero progetto è gestito tramite Maven e, tramite un dockerfile, è possibile compilare ed eseguire l'applicazione in un container Docker. 

## Compilazione

Tramite un Dockerfile è possibile costruire un container con le librerie necessarie al funzionamento dell'applicativo. Per creare
tale container bisogna eseguire la seguente istruzione tramite terminale nel path della cartella del progetto con Docker avviato : 

```docker build --no-cache -t sudoku-game  .```

## Esecuzione

Una volta fatto ciò, è possibile avviare il nodo master (MASTER-PEER) eseguendo la seguente istruzione:

```docker run -i --name MASTER-PEER -m MASTERIP="127.0.0.1" -id 0 sudoku-game```

L'istruzione non fa altro che eseguire il container partendo dall'immagine costruita precedentemente definendo il nome del container (MASTER-PEER), 
l'ip della rete (127.0.0.1) e l'ID del Master-peer (0). 


Dopo il primo lancio, è possibile avviare il master peer con il seguente comando: 

```docker start -i MASTER-PEER```.

Una volta avviato il Master-peer è possibile avviare gli altri peer. Ognuno di essi dovrà collegarsi alla rete
tramite l'indirizzo IP del container utilizzando un nome e un valore dell'ID univoci eseguendo la seguente istruzione:

```docker run -i --name PEER-X -m MASTERIP="172.17.0.2" -id X sudoku-game```

dove "X" rappresenta il valore dell'ID del peer. 

Dopo il primo lancio è possibile avviare il peer con la seguente istruzione: 

```docker start -i PEER-X```

dove "X" rappresenta il valore dell'ID del peer.

## Demo

