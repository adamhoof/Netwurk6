# SpudrNet6

## Téma a specifikace funkcionality

SpudrNet6 je intuitivní nástroj pro simulaci síťové komunikace, který je navržen pro vizualizaci a experimentování se základy síťových prostředí malého a středního rozsahu. Nabízí simulaci v reálném čase, dynamické chování a přizpůsobitelné funkce. Cílem je zlepšení porozumění uživatelů základím síťovým komunikačním principům.

### GUI

- **Umístění prvků**: Uživatelé mohou na mřížku umístit síťové prvky, jako jsou PC, routery a switche, a uspořádat tak topologii sítě.
- **Spojení prvků**: Vytváření spojení mezi prvky je možné táhnutím čar, které simulují síťové připojení (kabel nebo wireless, to simulace neřeší).
- **Ovládání simulace**: Uživatelé mají možnost jak spustit a zastavit simulaci, tak upravit její rychlost.
- **Vizualizace provozu**: Simulace vizualizuje pohyb packetů podél spojení, čímž indikuje aktuální tok dat v reálném čase.

### Konfigurace

- **Adresování IP**:
  - **WAN Routery**: Uživatelé přiřazují veřejné IP adresy routerům.
  - **PC v LAN**: PC v lokálních sítích získávají své IP adresy prostřednictvím vestavěného DHCP serveru v routeru, simulujícího proces DORA (Discover, Offer, Request, Acknowledgment).
- **Routování**: Základní routování je zajištěno statickou routovací tabulkou, nebo dynamickým protokolem RIP (Routing Information Protocol).
- **Switching**: Switche udržují dynamickou CAM (Content Addressable Memory) tabulku pro efektivní přeposílání packetů na základě MAC adres.
- **NAT & Port Forwarding**: Pro simulaci reálné konektivity v rámci WAN implementují border routery NAT (Network Address Translation) a port forwarding.

### Další funkce, abstrakce, omezení

- **Abstrakce packetů a rámců**: Pro zjednodušení a zaměření se na síťové koncepty simulace abstrahuje od složitostí skutečných packetů a rámců a posílají se tzv. zprávy - lze například odeslat celý text najednou, bez jakékoli segmentace.
- **Ztrátovost packetů**: Simulace nedisponuje funkcionalitou ztráty packetů nebo problémů s přetížením.
- **Role a komunikace**: Uživatelé se mohou ujmout role PC a iniciovat výměnu zpráv s ostatními PC v síti.
