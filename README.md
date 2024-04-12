# SpudrNet6

## Téma a specifikace funkcionality

SpudrNet6 je intuitivní nástroj pro simulaci síťové komunikace, který je navržen pro vizualizaci a experimentování se základy síťových prostředí malého a středního rozsahu. Nabízí simulaci v reálném čase, dynamické chování a přizpůsobitelné funkce. Cílem je zlepšení porozumění uživatelů základím síťovým komunikačním principům.

### GUI

- **Umístění prvků**: Uživatelé mohou na mřížku umístit síťové prvky, jmenovitě PC, routery a switche, a uspořádat tak topologii sítě.
- **Spojení prvků**: Vytváření spojení mezi prvky je možné zvolením funkce Connector a kliknutím na 2 požadované zařízení.
- **Ovládání simulace**: Uživatelé mají možnost spustit a pozastavit simulaci,
- **Vizualizace provozu**: Simulace vizualizuje pohyb packetů podél spojení, čímž indikuje aktuální tok dat v reálném čase.

### Funkcionalita

- **Tvorba sítí a subnetů**
  - **LAN**: Položením routeru se vytvoří defaultní LAN, to je defaultní subnet.
  - **Subnetting**: Připojením switche k routeru se vytvoří nový subnet, který router spravuje. Propojením PC a routeru nový subnet nevzniká, PC se přídá do defaultního subnetu. 
  - **WAN**: Při spojení dvou routerů se vytvoří WAN.
  - **PC v LAN**: PC v lokálních sítích získávají své IP adresy prostřednictvím vestavěného DHCP serveru v routeru, simulujícího proces DORA (Discover, Offer, Request, Acknowledgment).
- **Konfigurace zařízení**
  - **Router**: Router je v základu nakonfigurován (automatická správa subnetů, routovací tabulky, arp cache, rozhraní)
  - **PC**: Aby PC mohl participovat v LAN komunikaci, musí projít DORA procesem (automatická DHCP konfigurace). Svou IP adresu v síti tedy získa tímto způsobem a poté může komunikovat s libovolným pc v LAN síti.
  - **Switch**: Není nutné konfigurovat, automaticky si udržuje CAM tabulku.
- **Funkce simulace**
  - **Routování mezi WAN**: Je zajištěno dynamickým protokolem RIP (Routing Information Protocol), který se spouští každých 30 sekund.
  - **Switching**: Switche udržují dynamickou CAM (Content Addressable Memory) tabulku pro efektivní přeposílání packetů na základě MAC adres.
  - **Komunikace v LAN**: Vybírá se náhodná komunikace mezi 2 PCs. Pokud oba prošly DHCP konfigurací, komunikace se zahájí a síťové prvky zajistí správné doručení zprávy koncovému PC.

