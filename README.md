# SpudrNet6

## Téma a specifikace funkcionality

SpudrNet6 je intuitivní nástroj pro simulaci síťové komunikace, který je navržen pro vizualizaci a experimentování se základy síťových prostředí malého a středního rozsahu. Nabízí simulaci v reálném čase, dynamické chování a přizpůsobitelné funkce. Cílem je zlepšení porozumění uživatelů základím síťovým komunikačním principům.

### Funkcionalita

- **GUI**
  - **Umístění prvků**: Simulace umožňuje na pracovní plochu aplikace umístit síťové prvky, jmenovitě PC, routery a switche, pomocí "PC", "Router, "Switch" tlačítek.
  - **Import a export**: Konfiguraci sítě lze exportovat pomocí menu "Options"->"Save" a poté znovu ji zpátky importovat z hlavní stránky tlačítkem "Load" (ukázkové konfigurace jsou ve složce main/resources.).
  - **Propojení prvků**: Vytváření spojení mezi prvky je možné kliknutím na tlačítko "Connector" a kliknutím na 2 požadované zařízení a uspořádat tak topologii sítě.
  - **Flow simulace**: Lze spustit a pozastavit simulaci tlačítky "Start" a "Pause".
  - **Vizualizace provozu**: Simulace vizualizuje pohyb packetů podél spojení, čímž indikuje aktuální tok dat v reálném čase.
  - **Log window**: Simulace obsahuje logovací okno, kde jsou vypsány právě probíhající síťové operace, či chybové hlášky (např. když se uživatel snaží propojit zařízení samo se sebou...).
- **Tvorba sítí a subnetů**
  - **LAN**: Položením routeru se vytvoří defaultní LAN pro tento router s defaultním subnet.
  - **Subnetting**: Připojením switche k routeru se vytvoří nový subnet, který router spravuje. Propojením PC a routeru nový subnet nevzniká, PC se přídá do zmíněného defaultního subnetu.
  - **WAN**: Při spojení dvou routerů se vytvoří WAN.
- **Konfigurace zařízení**
  - **Router**: Router je v základu nakonfigurován (automatická správa subnetů, routovací tabulky, arp cache, rozhraní).
  - **PC**: Aby PC mohl participovat v LAN komunikaci, musí projít DORA procesem (automatická DHCP konfigurace) a obdržet tak IP adresu.
  - **Switch**: Není nutné konfigurovat, automaticky si udržuje CAM tabulku.
- **Funkce simulace**
  - **Protokoly**: Simulace implementuje protokoly: RIP, DHCP, ARP, IP.
  - **Routování mezi WAN**: Je zajištěno dynamickým protokolem RIP (Routing Information Protocol), který se spouští každých 30 sekund.
  - **Switching**: Switche udržují dynamickou CAM (Content Addressable Memory) tabulku pro efektivní přeposílání packetů na základě MAC adres.
  - **Komunikace v LAN**: Vybírá se náhodná komunikace mezi 2 PCs. Pokud oba prošly DHCP konfigurací, komunikace se zahájí a síťové prvky zajistí správné doručení zprávy koncovému PC.
