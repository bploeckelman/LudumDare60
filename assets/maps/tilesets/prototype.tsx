<?xml version="1.0" encoding="UTF-8"?>
<tileset version="1.10" tiledversion="1.12.2" name="prototype" tilewidth="16" tileheight="16" tilecount="256" columns="16">
 <image source="prototype.png" width="256" height="256"/>
 <tile id="0" type="solid">
  <properties>
   <property name="collision" value="solid"/>
   <property name="kind" value="solid"/>
  </properties>
  <objectgroup draworder="index" id="2">
   <object id="1" x="0" y="0" width="16" height="16"/>
  </objectgroup>
 </tile>
 <tile id="1" type="slope_br">
  <properties>
   <property name="collision" value="slope"/>
   <property name="kind" value="slope_br"/>
   <property name="slope" value="br"/>
  </properties>
  <objectgroup draworder="index" id="2">
   <object id="1" x="0" y="0">
    <polygon points="0,16 16,0 16,16"/>
   </object>
  </objectgroup>
 </tile>
 <tile id="2" type="slope_bl">
  <properties>
   <property name="collision" value="slope"/>
   <property name="kind" value="slope_bl"/>
   <property name="slope" value="bl"/>
  </properties>
  <objectgroup draworder="index" id="2">
   <object id="1" x="0" y="0">
    <polygon points="16,16 0,0 0,16"/>
   </object>
  </objectgroup>
 </tile>
 <tile id="3" type="slope_tr">
  <properties>
   <property name="collision" value="slope"/>
   <property name="kind" value="slope_tr"/>
   <property name="slope" value="tr"/>
  </properties>
  <objectgroup draworder="index" id="2">
   <object id="1" x="0" y="0">
    <polygon points="0,0 16,16 16,0"/>
   </object>
  </objectgroup>
 </tile>
 <tile id="4" type="slope_tl">
  <properties>
   <property name="collision" value="slope"/>
   <property name="kind" value="slope_tl"/>
   <property name="slope" value="tl"/>
  </properties>
  <objectgroup draworder="index" id="2">
   <object id="1" x="0" y="0">
    <polygon points="16,0 0,16 0,0"/>
   </object>
  </objectgroup>
 </tile>
 <tile id="5" type="slope_br_low_a">
  <properties>
   <property name="collision" value="slope"/>
   <property name="kind" value="slope_br_low_a"/>
   <property name="slope" value="br_low_a"/>
  </properties>
  <objectgroup draworder="index" id="2">
   <object id="1" x="0" y="0">
    <polygon points="0,16 16,8 16,16"/>
   </object>
  </objectgroup>
 </tile>
 <tile id="6" type="slope_br_low_b">
  <properties>
   <property name="collision" value="slope"/>
   <property name="kind" value="slope_br_low_b"/>
   <property name="slope" value="br_low_b"/>
  </properties>
  <objectgroup draworder="index" id="2">
   <object id="1" x="0" y="0">
    <polygon points="0,8 16,0 16,16 0,16"/>
   </object>
  </objectgroup>
 </tile>
 <tile id="7" type="slope_bl_low_b">
  <properties>
   <property name="collision" value="slope"/>
   <property name="kind" value="slope_bl_low_b"/>
   <property name="slope" value="bl_low_b"/>
  </properties>
  <objectgroup draworder="index" id="2">
   <object id="1" x="0" y="0">
    <polygon points="16,8 0,0 0,16 16,16"/>
   </object>
  </objectgroup>
 </tile>
 <tile id="8" type="slope_bl_low_a">
  <properties>
   <property name="collision" value="slope"/>
   <property name="kind" value="slope_bl_low_a"/>
   <property name="slope" value="bl_low_a"/>
  </properties>
  <objectgroup draworder="index" id="2">
   <object id="1" x="0" y="0">
    <polygon points="16,16 0,8 0,16"/>
   </object>
  </objectgroup>
 </tile>
 <tile id="9" type="slope_tr_low_a">
  <properties>
   <property name="collision" value="slope"/>
   <property name="kind" value="slope_tr_low_a"/>
   <property name="slope" value="tr_low_a"/>
  </properties>
  <objectgroup draworder="index" id="2">
   <object id="1" x="0" y="0">
    <polygon points="0,0 16,8 16,0"/>
   </object>
  </objectgroup>
 </tile>
 <tile id="10" type="slope_tr_low_b">
  <properties>
   <property name="collision" value="slope"/>
   <property name="kind" value="slope_tr_low_b"/>
   <property name="slope" value="tr_low_b"/>
  </properties>
  <objectgroup draworder="index" id="2">
   <object id="1" x="0" y="0">
    <polygon points="0,8 16,16 16,0 0,0"/>
   </object>
  </objectgroup>
 </tile>
 <tile id="11" type="slope_tl_low_b">
  <properties>
   <property name="collision" value="slope"/>
   <property name="kind" value="slope_tl_low_b"/>
   <property name="slope" value="tl_low_b"/>
  </properties>
  <objectgroup draworder="index" id="2">
   <object id="1" x="0" y="0">
    <polygon points="16,8 0,16 0,0 16,0"/>
   </object>
  </objectgroup>
 </tile>
 <tile id="12" type="slope_tl_low_a">
  <properties>
   <property name="collision" value="slope"/>
   <property name="kind" value="slope_tl_low_a"/>
   <property name="slope" value="tl_low_a"/>
  </properties>
  <objectgroup draworder="index" id="2">
   <object id="1" x="0" y="0">
    <polygon points="16,0 0,8 0,0"/>
   </object>
  </objectgroup>
 </tile>
 <tile id="13" type="bg_block">
  <properties>
   <property name="collision" value="none"/>
   <property name="kind" value="bg_block"/>
  </properties>
 </tile>
 <tile id="14" type="bg_wall">
  <properties>
   <property name="collision" value="none"/>
   <property name="kind" value="bg_wall"/>
  </properties>
 </tile>
 <tile id="16" type="half_top">
  <properties>
   <property name="collision" value="solid"/>
   <property name="kind" value="half_top"/>
  </properties>
  <objectgroup draworder="index" id="2">
   <object id="1" x="0" y="0" width="16" height="8"/>
  </objectgroup>
 </tile>
 <tile id="17" type="half_bottom">
  <properties>
   <property name="collision" value="solid"/>
   <property name="kind" value="half_bottom"/>
  </properties>
  <objectgroup draworder="index" id="2">
   <object id="1" x="0" y="8" width="16" height="8"/>
  </objectgroup>
 </tile>
 <tile id="18" type="half_left">
  <properties>
   <property name="collision" value="solid"/>
   <property name="kind" value="half_left"/>
  </properties>
  <objectgroup draworder="index" id="2">
   <object id="1" x="0" y="0" width="8" height="16"/>
  </objectgroup>
 </tile>
 <tile id="19" type="half_right">
  <properties>
   <property name="collision" value="solid"/>
   <property name="kind" value="half_right"/>
  </properties>
  <objectgroup draworder="index" id="2">
   <object id="1" x="8" y="0" width="8" height="16"/>
  </objectgroup>
 </tile>
 <tile id="20" type="oneway_top">
  <properties>
   <property name="collision" value="oneway"/>
   <property name="kind" value="oneway_top"/>
  </properties>
  <objectgroup draworder="index" id="2">
   <object id="1" x="0" y="0" width="16" height="2"/>
  </objectgroup>
 </tile>
 <tile id="21" type="oneway_mid">
  <properties>
   <property name="collision" value="oneway"/>
   <property name="kind" value="oneway_mid"/>
  </properties>
  <objectgroup draworder="index" id="2">
   <object id="1" x="0" y="7" width="16" height="2"/>
  </objectgroup>
 </tile>
 <tile id="22" type="oneway_bottom">
  <properties>
   <property name="collision" value="oneway"/>
   <property name="kind" value="oneway_bottom"/>
  </properties>
  <objectgroup draworder="index" id="2">
   <object id="1" x="0" y="14" width="16" height="2"/>
  </objectgroup>
 </tile>
 <tile id="23" type="ladder">
  <properties>
   <property name="collision" value="ladder"/>
   <property name="kind" value="ladder"/>
  </properties>
 </tile>
 <tile id="24" type="ladder_top">
  <properties>
   <property name="collision" value="ladder_top"/>
   <property name="kind" value="ladder_top"/>
  </properties>
  <objectgroup draworder="index" id="2">
   <object id="1" x="0" y="0" width="16" height="2"/>
  </objectgroup>
 </tile>
 <tile id="25" type="breakable">
  <properties>
   <property name="collision" value="solid"/>
   <property name="kind" value="breakable"/>
  </properties>
  <objectgroup draworder="index" id="2">
   <object id="1" x="0" y="0" width="16" height="16"/>
  </objectgroup>
 </tile>
 <tile id="26" type="crumble">
  <properties>
   <property name="collision" value="solid"/>
   <property name="kind" value="crumble"/>
  </properties>
  <objectgroup draworder="index" id="2">
   <object id="1" x="0" y="0" width="16" height="8"/>
  </objectgroup>
 </tile>
 <tile id="27" type="ice">
  <properties>
   <property name="collision" value="solid"/>
   <property name="kind" value="ice"/>
  </properties>
  <objectgroup draworder="index" id="2">
   <object id="1" x="0" y="0" width="16" height="16"/>
  </objectgroup>
 </tile>
 <tile id="28" type="conveyor_left">
  <properties>
   <property name="collision" value="solid"/>
   <property name="dir" value="left"/>
   <property name="kind" value="conveyor_left"/>
  </properties>
  <objectgroup draworder="index" id="2">
   <object id="1" x="0" y="0" width="16" height="16"/>
  </objectgroup>
 </tile>
 <tile id="29" type="conveyor_right">
  <properties>
   <property name="collision" value="solid"/>
   <property name="dir" value="right"/>
   <property name="kind" value="conveyor_right"/>
  </properties>
  <objectgroup draworder="index" id="2">
   <object id="1" x="0" y="0" width="16" height="16"/>
  </objectgroup>
 </tile>
 <tile id="32" type="spikes_up">
  <properties>
   <property name="collision" value="hazard"/>
   <property name="dir" value="up"/>
   <property name="kind" value="spikes_up"/>
  </properties>
  <objectgroup draworder="index" id="2">
   <object id="1" x="0" y="4" width="16" height="12"/>
  </objectgroup>
 </tile>
 <tile id="33" type="spikes_right">
  <properties>
   <property name="collision" value="hazard"/>
   <property name="dir" value="right"/>
   <property name="kind" value="spikes_right"/>
  </properties>
  <objectgroup draworder="index" id="2">
   <object id="1" x="0" y="0" width="12" height="16"/>
  </objectgroup>
 </tile>
 <tile id="34" type="spikes_down">
  <properties>
   <property name="collision" value="hazard"/>
   <property name="dir" value="down"/>
   <property name="kind" value="spikes_down"/>
  </properties>
  <objectgroup draworder="index" id="2">
   <object id="1" x="0" y="0" width="16" height="12"/>
  </objectgroup>
 </tile>
 <tile id="35" type="spikes_left">
  <properties>
   <property name="collision" value="hazard"/>
   <property name="dir" value="left"/>
   <property name="kind" value="spikes_left"/>
  </properties>
  <objectgroup draworder="index" id="2">
   <object id="1" x="4" y="0" width="12" height="16"/>
  </objectgroup>
 </tile>
 <tile id="36" type="kill_zone">
  <properties>
   <property name="collision" value="hazard"/>
   <property name="kind" value="kill_zone"/>
  </properties>
  <objectgroup draworder="index" id="2">
   <object id="1" x="0" y="0" width="16" height="16"/>
  </objectgroup>
 </tile>
 <tile id="37" type="water_surface">
  <properties>
   <property name="collision" value="liquid"/>
   <property name="kind" value="water_surface"/>
  </properties>
  <objectgroup draworder="index" id="2">
   <object id="1" x="0" y="4" width="16" height="12"/>
  </objectgroup>
 </tile>
 <tile id="38" type="water">
  <properties>
   <property name="collision" value="liquid"/>
   <property name="kind" value="water"/>
  </properties>
  <objectgroup draworder="index" id="2">
   <object id="1" x="0" y="0" width="16" height="16"/>
  </objectgroup>
 </tile>
 <tile id="39" type="lava_surface">
  <properties>
   <property name="collision" value="hazard"/>
   <property name="kind" value="lava_surface"/>
  </properties>
  <objectgroup draworder="index" id="2">
   <object id="1" x="0" y="4" width="16" height="12"/>
  </objectgroup>
 </tile>
 <tile id="40" type="lava">
  <properties>
   <property name="collision" value="hazard"/>
   <property name="kind" value="lava"/>
  </properties>
  <objectgroup draworder="index" id="2">
   <object id="1" x="0" y="0" width="16" height="16"/>
  </objectgroup>
 </tile>
 <tile id="48" type="player_spawn">
  <properties>
   <property name="kind" value="player_spawn"/>
  </properties>
 </tile>
 <tile id="49" type="checkpoint">
  <properties>
   <property name="kind" value="checkpoint"/>
  </properties>
 </tile>
 <tile id="50" type="goal">
  <properties>
   <property name="kind" value="goal"/>
  </properties>
 </tile>
 <tile id="51" type="npc">
  <properties>
   <property name="kind" value="npc"/>
  </properties>
 </tile>
 <tile id="52" type="interact">
  <properties>
   <property name="kind" value="interact"/>
  </properties>
 </tile>
 <tile id="53" type="trigger">
  <properties>
   <property name="kind" value="trigger"/>
  </properties>
 </tile>
 <tile id="54" type="bounce_pad">
  <properties>
   <property name="kind" value="bounce_pad"/>
  </properties>
 </tile>
 <tile id="55" type="coin">
  <properties>
   <property name="kind" value="coin"/>
  </properties>
 </tile>
 <tile id="56" type="gem">
  <properties>
   <property name="kind" value="gem"/>
  </properties>
 </tile>
 <tile id="57" type="health">
  <properties>
   <property name="kind" value="health"/>
  </properties>
 </tile>
 <tile id="58" type="key">
  <properties>
   <property name="kind" value="key"/>
  </properties>
 </tile>
 <tile id="59" type="moving_platform_left">
  <properties>
   <property name="collision" value="solid"/>
   <property name="kind" value="moving_platform_left"/>
  </properties>
  <objectgroup draworder="index" id="2">
   <object id="1" x="0" y="0" width="16" height="7"/>
  </objectgroup>
 </tile>
 <tile id="60" type="moving_platform_mid">
  <properties>
   <property name="collision" value="solid"/>
   <property name="kind" value="moving_platform_mid"/>
  </properties>
  <objectgroup draworder="index" id="2">
   <object id="1" x="0" y="0" width="16" height="7"/>
  </objectgroup>
 </tile>
 <tile id="61" type="moving_platform_right">
  <properties>
   <property name="collision" value="solid"/>
   <property name="kind" value="moving_platform_right"/>
  </properties>
  <objectgroup draworder="index" id="2">
   <object id="1" x="0" y="0" width="16" height="7"/>
  </objectgroup>
 </tile>
 <tile id="62" type="path_node">
  <properties>
   <property name="kind" value="path_node"/>
  </properties>
 </tile>
 <tile id="63" type="patrol_limit">
  <properties>
   <property name="kind" value="patrol_limit"/>
  </properties>
 </tile>
 <tile id="64" type="enemy_blob">
  <properties>
   <property name="kind" value="enemy_blob"/>
  </properties>
 </tile>
 <tile id="65" type="enemy_walker">
  <properties>
   <property name="dir" value="right"/>
   <property name="kind" value="enemy_walker"/>
  </properties>
 </tile>
 <tile id="66" type="enemy_walker">
  <properties>
   <property name="dir" value="left"/>
   <property name="kind" value="enemy_walker"/>
  </properties>
 </tile>
 <tile id="67" type="enemy_shooter">
  <properties>
   <property name="dir" value="right"/>
   <property name="kind" value="enemy_shooter"/>
  </properties>
 </tile>
 <tile id="68" type="enemy_shooter">
  <properties>
   <property name="dir" value="left"/>
   <property name="kind" value="enemy_shooter"/>
  </properties>
 </tile>
 <tile id="69" type="enemy_shooter">
  <properties>
   <property name="dir" value="up"/>
   <property name="kind" value="enemy_shooter"/>
  </properties>
 </tile>
 <tile id="70" type="enemy_shooter">
  <properties>
   <property name="dir" value="down"/>
   <property name="kind" value="enemy_shooter"/>
  </properties>
 </tile>
 <tile id="71" type="enemy_flyer">
  <properties>
   <property name="kind" value="enemy_flyer"/>
  </properties>
 </tile>
 <tile id="72" type="lever">
  <properties>
   <property name="kind" value="lever"/>
   <property name="state" value="off"/>
  </properties>
 </tile>
 <tile id="73" type="lever">
  <properties>
   <property name="kind" value="lever"/>
   <property name="state" value="on"/>
  </properties>
 </tile>
 <tile id="74" type="pressure_plate">
  <properties>
   <property name="kind" value="pressure_plate"/>
   <property name="state" value="up"/>
  </properties>
 </tile>
 <tile id="75" type="pressure_plate">
  <properties>
   <property name="kind" value="pressure_plate"/>
   <property name="state" value="down"/>
  </properties>
 </tile>
 <tile id="76" type="crate">
  <properties>
   <property name="collision" value="solid"/>
   <property name="kind" value="crate"/>
  </properties>
  <objectgroup draworder="index" id="2">
   <object id="1" x="0" y="0" width="16" height="16"/>
  </objectgroup>
 </tile>
 <tile id="77" type="teleporter">
  <properties>
   <property name="kind" value="teleporter"/>
  </properties>
 </tile>
 <tile id="80" type="door_closed">
  <properties>
   <property name="collision" value="solid"/>
   <property name="kind" value="door_closed"/>
   <property name="part" value="top"/>
  </properties>
  <objectgroup draworder="index" id="2">
   <object id="1" x="0" y="0" width="16" height="16"/>
  </objectgroup>
 </tile>
 <tile id="81" type="door_open">
  <properties>
   <property name="collision" value="none"/>
   <property name="kind" value="door_open"/>
   <property name="part" value="top"/>
  </properties>
 </tile>
 <tile id="82" type="door_locked">
  <properties>
   <property name="collision" value="solid"/>
   <property name="kind" value="door_locked"/>
   <property name="part" value="top"/>
  </properties>
  <objectgroup draworder="index" id="2">
   <object id="1" x="0" y="0" width="16" height="16"/>
  </objectgroup>
 </tile>
 <tile id="96" type="door_closed">
  <properties>
   <property name="collision" value="solid"/>
   <property name="kind" value="door_closed"/>
   <property name="part" value="mid"/>
  </properties>
  <objectgroup draworder="index" id="2">
   <object id="1" x="0" y="0" width="16" height="16"/>
  </objectgroup>
 </tile>
 <tile id="97" type="door_open">
  <properties>
   <property name="collision" value="none"/>
   <property name="kind" value="door_open"/>
   <property name="part" value="mid"/>
  </properties>
 </tile>
 <tile id="98" type="door_locked">
  <properties>
   <property name="collision" value="solid"/>
   <property name="kind" value="door_locked"/>
   <property name="part" value="mid"/>
  </properties>
  <objectgroup draworder="index" id="2">
   <object id="1" x="0" y="0" width="16" height="16"/>
  </objectgroup>
 </tile>
 <tile id="112" type="door_closed">
  <properties>
   <property name="collision" value="solid"/>
   <property name="kind" value="door_closed"/>
   <property name="part" value="bottom"/>
  </properties>
  <objectgroup draworder="index" id="2">
   <object id="1" x="0" y="0" width="16" height="16"/>
  </objectgroup>
 </tile>
 <tile id="113" type="door_open">
  <properties>
   <property name="collision" value="none"/>
   <property name="kind" value="door_open"/>
   <property name="part" value="bottom"/>
  </properties>
 </tile>
 <tile id="114" type="door_locked">
  <properties>
   <property name="collision" value="solid"/>
   <property name="kind" value="door_locked"/>
   <property name="part" value="bottom"/>
  </properties>
  <objectgroup draworder="index" id="2">
   <object id="1" x="0" y="0" width="16" height="16"/>
  </objectgroup>
 </tile>
 <tile id="128" type="arrow">
  <properties>
   <property name="dir" value="n"/>
   <property name="kind" value="arrow"/>
  </properties>
 </tile>
 <tile id="129" type="arrow">
  <properties>
   <property name="dir" value="ne"/>
   <property name="kind" value="arrow"/>
  </properties>
 </tile>
 <tile id="130" type="arrow">
  <properties>
   <property name="dir" value="e"/>
   <property name="kind" value="arrow"/>
  </properties>
 </tile>
 <tile id="131" type="arrow">
  <properties>
   <property name="dir" value="se"/>
   <property name="kind" value="arrow"/>
  </properties>
 </tile>
 <tile id="132" type="arrow">
  <properties>
   <property name="dir" value="s"/>
   <property name="kind" value="arrow"/>
  </properties>
 </tile>
 <tile id="133" type="arrow">
  <properties>
   <property name="dir" value="sw"/>
   <property name="kind" value="arrow"/>
  </properties>
 </tile>
 <tile id="134" type="arrow">
  <properties>
   <property name="dir" value="w"/>
   <property name="kind" value="arrow"/>
  </properties>
 </tile>
 <tile id="135" type="arrow">
  <properties>
   <property name="dir" value="nw"/>
   <property name="kind" value="arrow"/>
  </properties>
 </tile>
 <tile id="144" type="id_tag">
  <properties>
   <property name="id" type="int" value="0"/>
   <property name="kind" value="id_tag"/>
  </properties>
 </tile>
 <tile id="145" type="id_tag">
  <properties>
   <property name="id" type="int" value="1"/>
   <property name="kind" value="id_tag"/>
  </properties>
 </tile>
 <tile id="146" type="id_tag">
  <properties>
   <property name="id" type="int" value="2"/>
   <property name="kind" value="id_tag"/>
  </properties>
 </tile>
 <tile id="147" type="id_tag">
  <properties>
   <property name="id" type="int" value="3"/>
   <property name="kind" value="id_tag"/>
  </properties>
 </tile>
 <tile id="148" type="id_tag">
  <properties>
   <property name="id" type="int" value="4"/>
   <property name="kind" value="id_tag"/>
  </properties>
 </tile>
 <tile id="149" type="id_tag">
  <properties>
   <property name="id" type="int" value="5"/>
   <property name="kind" value="id_tag"/>
  </properties>
 </tile>
 <tile id="150" type="id_tag">
  <properties>
   <property name="id" type="int" value="6"/>
   <property name="kind" value="id_tag"/>
  </properties>
 </tile>
 <tile id="151" type="id_tag">
  <properties>
   <property name="id" type="int" value="7"/>
   <property name="kind" value="id_tag"/>
  </properties>
 </tile>
 <tile id="152" type="id_tag">
  <properties>
   <property name="id" type="int" value="8"/>
   <property name="kind" value="id_tag"/>
  </properties>
 </tile>
 <tile id="153" type="id_tag">
  <properties>
   <property name="id" type="int" value="9"/>
   <property name="kind" value="id_tag"/>
  </properties>
 </tile>
 <tile id="154" type="id_tag">
  <properties>
   <property name="id" type="int" value="10"/>
   <property name="kind" value="id_tag"/>
  </properties>
 </tile>
 <tile id="155" type="id_tag">
  <properties>
   <property name="id" type="int" value="11"/>
   <property name="kind" value="id_tag"/>
  </properties>
 </tile>
 <tile id="156" type="id_tag">
  <properties>
   <property name="id" type="int" value="12"/>
   <property name="kind" value="id_tag"/>
  </properties>
 </tile>
 <tile id="157" type="id_tag">
  <properties>
   <property name="id" type="int" value="13"/>
   <property name="kind" value="id_tag"/>
  </properties>
 </tile>
 <tile id="158" type="id_tag">
  <properties>
   <property name="id" type="int" value="14"/>
   <property name="kind" value="id_tag"/>
  </properties>
 </tile>
 <tile id="159" type="id_tag">
  <properties>
   <property name="id" type="int" value="15"/>
   <property name="kind" value="id_tag"/>
  </properties>
 </tile>
 <tile id="160" type="region">
  <properties>
   <property name="color" value="red"/>
   <property name="kind" value="region"/>
  </properties>
 </tile>
 <tile id="161" type="region">
  <properties>
   <property name="color" value="orange"/>
   <property name="kind" value="region"/>
  </properties>
 </tile>
 <tile id="162" type="region">
  <properties>
   <property name="color" value="yellow"/>
   <property name="kind" value="region"/>
  </properties>
 </tile>
 <tile id="163" type="region">
  <properties>
   <property name="color" value="green"/>
   <property name="kind" value="region"/>
  </properties>
 </tile>
 <tile id="164" type="region">
  <properties>
   <property name="color" value="cyan"/>
   <property name="kind" value="region"/>
  </properties>
 </tile>
 <tile id="165" type="region">
  <properties>
   <property name="color" value="blue"/>
   <property name="kind" value="region"/>
  </properties>
 </tile>
 <tile id="166" type="region">
  <properties>
   <property name="color" value="purple"/>
   <property name="kind" value="region"/>
  </properties>
 </tile>
 <tile id="167" type="region">
  <properties>
   <property name="color" value="pink"/>
   <property name="kind" value="region"/>
  </properties>
 </tile>
</tileset>
