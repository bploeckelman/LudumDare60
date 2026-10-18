<?xml version="1.0" encoding="UTF-8"?>
<tileset version="1.10" tiledversion="1.12.2" name="prototype" tilewidth="16" tileheight="16" tilecount="256" columns="16">
 <image source="prototype.png" width="256" height="256"/>
 <tile id="0" type="solid">
  <properties>
   <property name="shape" value="full"/>
  </properties>
 </tile>
 <tile id="1" type="slope_br">
  <properties>
   <property name="shape" value="ramp_ne"/>
  </properties>
 </tile>
 <tile id="2" type="slope_bl">
  <properties>
   <property name="shape" value="ramp_nw"/>
  </properties>
 </tile>
 <tile id="3" type="slope_tr">
  <properties>
   <property name="shape" value="ramp_se"/>
  </properties>
 </tile>
 <tile id="4" type="slope_tl">
  <properties>
   <property name="shape" value="ramp_sw"/>
  </properties>
 </tile>
 <tile id="5" type="slope_br_low_a">
  <properties>
  </properties>
 </tile>
 <tile id="6" type="slope_br_low_b">
  <properties>
  </properties>
 </tile>
 <tile id="7" type="slope_bl_low_b">
  <properties>
  </properties>
 </tile>
 <tile id="8" type="slope_bl_low_a">
  <properties>
  </properties>
 </tile>
 <tile id="9" type="slope_tr_low_a">
  <properties>
  </properties>
 </tile>
 <tile id="10" type="slope_tr_low_b">
  <properties>
  </properties>
 </tile>
 <tile id="11" type="slope_tl_low_b">
  <properties>
  </properties>
 </tile>
 <tile id="12" type="slope_tl_low_a">
  <properties>
  </properties>
 </tile>
 <tile id="13" type="bg_block">
  <properties>
  </properties>
 </tile>
 <tile id="14" type="bg_wall">
  <properties>
  </properties>
 </tile>
 <tile id="16" type="half_top">
  <properties>
  </properties>
 </tile>
 <tile id="17" type="half_bottom">
  <properties>
  </properties>
 </tile>
 <tile id="18" type="half_left">
  <properties>
  </properties>
 </tile>
 <tile id="19" type="half_right">
  <properties>
  </properties>
 </tile>
 <tile id="20" type="oneway_top">
  <properties>
  </properties>
 </tile>
 <tile id="21" type="oneway_mid">
  <properties>
  </properties>
 </tile>
 <tile id="22" type="oneway_bottom">
  <properties>
  </properties>
 </tile>
 <tile id="23" type="ladder">
  <properties>
  </properties>
 </tile>
 <tile id="24" type="ladder_top">
  <properties>
  </properties>
 </tile>
 <tile id="25" type="breakable">
  <properties>
  </properties>
 </tile>
 <tile id="26" type="crumble">
  <properties>
  </properties>
 </tile>
 <tile id="27" type="ice">
  <properties>
  </properties>
 </tile>
 <tile id="28" type="conveyor_left">
  <properties>
  </properties>
 </tile>
 <tile id="29" type="conveyor_right">
  <properties>
  </properties>
 </tile>
 <tile id="32" type="spikes_up">
  <properties>
   <property name="collision" value="hazard"/>
   <property name="direction" value="up"/>
  </properties>
 </tile>
 <tile id="33" type="spikes_right">
  <properties>
   <property name="collision" value="hazard"/>
   <property name="direction" value="right"/>
  </properties>
 </tile>
 <tile id="34" type="spikes_down">
  <properties>
   <property name="collision" value="hazard"/>
   <property name="direction" value="down"/>
  </properties>
 </tile>
 <tile id="35" type="spikes_left">
  <properties>
   <property name="collision" value="hazard"/>
   <property name="direction" value="left"/>
  </properties>
 </tile>
 <tile id="36" type="kill_zone">
  <properties>
   <property name="collision" value="hazard"/>
  </properties>
 </tile>
 <tile id="37" type="water_surface">
  <properties>
   <property name="collision" value="hazard"/>
  </properties>
 </tile>
 <tile id="38" type="water">
  <properties>
   <property name="collision" value="hazard"/>
  </properties>
 </tile>
 <tile id="39" type="lava_surface">
  <properties>
   <property name="collision" value="hazard"/>
  </properties>
 </tile>
 <tile id="40" type="lava">
  <properties>
   <property name="collision" value="hazard"/>
  </properties>
 </tile>
 <tile id="48" type="player_spawn">
  <properties>
  </properties>
 </tile>
 <tile id="49" type="checkpoint">
  <properties>
  </properties>
 </tile>
 <tile id="50" type="goal">
  <properties>
  </properties>
 </tile>
 <tile id="51" type="npc">
  <properties>
  </properties>
 </tile>
 <tile id="52" type="interact">
  <properties>
  </properties>
 </tile>
 <tile id="53" type="trigger">
  <properties>
  </properties>
 </tile>
 <tile id="54" type="bounce_pad">
  <properties>
  </properties>
 </tile>
 <tile id="55" type="coin">
  <properties>
  </properties>
 </tile>
 <tile id="56" type="gem">
  <properties>
  </properties>
 </tile>
 <tile id="57" type="health">
  <properties>
  </properties>
 </tile>
 <tile id="58" type="key">
  <properties>
  </properties>
 </tile>
 <tile id="59" type="moving_platform_left">
  <properties>
   <property name="collision" value="solid"/>
  </properties>
 </tile>
 <tile id="60" type="moving_platform_mid">
  <properties>
   <property name="collision" value="solid"/>
  </properties>
 </tile>
 <tile id="61" type="moving_platform_right">
  <properties>
   <property name="collision" value="solid"/>
  </properties>
 </tile>
 <tile id="62" type="path_node">
  <properties>
  </properties>
 </tile>
 <tile id="63" type="patrol_limit">
  <properties>
  </properties>
 </tile>
 <tile id="64" type="enemy_blob">
  <properties>
  </properties>
 </tile>
 <tile id="65" type="enemy_walker">
  <properties>
   <property name="direction" value="right"/>
  </properties>
 </tile>
 <tile id="66" type="enemy_walker">
  <properties>
   <property name="direction" value="left"/>
  </properties>
 </tile>
 <tile id="67" type="enemy_shooter">
  <properties>
   <property name="direction" value="right"/>
  </properties>
 </tile>
 <tile id="68" type="enemy_shooter">
  <properties>
   <property name="direction" value="left"/>
  </properties>
 </tile>
 <tile id="69" type="enemy_shooter">
  <properties>
   <property name="direction" value="up"/>
  </properties>
 </tile>
 <tile id="70" type="enemy_shooter">
  <properties>
   <property name="direction" value="down"/>
  </properties>
 </tile>
 <tile id="71" type="enemy_flyer">
  <properties>
  </properties>
 </tile>
 <tile id="72" type="lever">
  <properties>
   <property name="state" value="off"/>
  </properties>
 </tile>
 <tile id="73" type="lever">
  <properties>
   <property name="state" value="on"/>
  </properties>
 </tile>
 <tile id="74" type="pressure_plate">
  <properties>
   <property name="state" value="up"/>
  </properties>
 </tile>
 <tile id="75" type="pressure_plate">
  <properties>
   <property name="state" value="down"/>
  </properties>
 </tile>
 <tile id="76" type="crate">
  <properties>
   <property name="shape" value="full"/>
  </properties>
 </tile>
 <tile id="77" type="teleporter">
  <properties>
  </properties>
 </tile>
 <tile id="80" type="door_closed">
  <properties>
   <property name="collision" value="full"/>
   <property name="kind" value="door_closed"/>
   <property name="part" value="top"/>
  </properties>
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
   <property name="collision" value="full"/>
   <property name="kind" value="door_locked"/>
   <property name="part" value="top"/>
  </properties>
 </tile>
 <tile id="96" type="door_closed">
  <properties>
   <property name="collision" value="full"/>
   <property name="kind" value="door_closed"/>
   <property name="part" value="mid"/>
  </properties>
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
   <property name="collision" value="full"/>
   <property name="kind" value="door_locked"/>
   <property name="part" value="mid"/>
  </properties>
 </tile>
 <tile id="112" type="door_closed">
  <properties>
   <property name="collision" value="full"/>
   <property name="kind" value="door_closed"/>
   <property name="part" value="bottom"/>
  </properties>
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
   <property name="collision" value="full"/>
   <property name="kind" value="door_locked"/>
   <property name="part" value="bottom"/>
  </properties>
 </tile>
 <tile id="128" type="arrow">
  <properties>
   <property name="direction" value="n"/>
  </properties>
 </tile>
 <tile id="129" type="arrow">
  <properties>
   <property name="direction" value="ne"/>
  </properties>
 </tile>
 <tile id="130" type="arrow">
  <properties>
   <property name="direction" value="e"/>
  </properties>
 </tile>
 <tile id="131" type="arrow">
  <properties>
   <property name="direction" value="se"/>
  </properties>
 </tile>
 <tile id="132" type="arrow">
  <properties>
   <property name="direction" value="s"/>
  </properties>
 </tile>
 <tile id="133" type="arrow">
  <properties>
   <property name="direction" value="sw"/>
  </properties>
 </tile>
 <tile id="134" type="arrow">
  <properties>
   <property name="diretionc" value="w"/>
  </properties>
 </tile>
 <tile id="135" type="arrow">
  <properties>
   <property name="direction" value="nw"/>
  </properties>
 </tile>
 <tile id="144" type="id_tag">
  <properties>
   <property name="id" type="int" value="0"/>
  </properties>
 </tile>
 <tile id="145" type="id_tag">
  <properties>
   <property name="id" type="int" value="1"/>
  </properties>
 </tile>
 <tile id="146" type="id_tag">
  <properties>
   <property name="id" type="int" value="2"/>
  </properties>
 </tile>
 <tile id="147" type="id_tag">
  <properties>
   <property name="id" type="int" value="3"/>
  </properties>
 </tile>
 <tile id="148" type="id_tag">
  <properties>
   <property name="id" type="int" value="4"/>
  </properties>
 </tile>
 <tile id="149" type="id_tag">
  <properties>
   <property name="id" type="int" value="5"/>
  </properties>
 </tile>
 <tile id="150" type="id_tag">
  <properties>
   <property name="id" type="int" value="6"/>
  </properties>
 </tile>
 <tile id="151" type="id_tag">
  <properties>
   <property name="id" type="int" value="7"/>
  </properties>
 </tile>
 <tile id="152" type="id_tag">
  <properties>
   <property name="id" type="int" value="8"/>
  </properties>
 </tile>
 <tile id="153" type="id_tag">
  <properties>
   <property name="id" type="int" value="9"/>
  </properties>
 </tile>
 <tile id="154" type="id_tag">
  <properties>
   <property name="id" type="int" value="10"/>
  </properties>
 </tile>
 <tile id="155" type="id_tag">
  <properties>
   <property name="id" type="int" value="11"/>
  </properties>
 </tile>
 <tile id="156" type="id_tag">
  <properties>
   <property name="id" type="int" value="12"/>
  </properties>
 </tile>
 <tile id="157" type="id_tag">
  <properties>
   <property name="id" type="int" value="13"/>
  </properties>
 </tile>
 <tile id="158" type="id_tag">
  <properties>
   <property name="id" type="int" value="14"/>
  </properties>
 </tile>
 <tile id="159" type="id_tag">
  <properties>
   <property name="id" type="int" value="15"/>
  </properties>
 </tile>
 <tile id="160" type="region">
  <properties>
   <property name="shape" value="full"/>
   <property name="color" value="red"/>
  </properties>
 </tile>
 <tile id="161" type="region">
  <properties>
   <property name="shape" value="full"/>
   <property name="color" value="orange"/>
  </properties>
 </tile>
 <tile id="162" type="region">
  <properties>
   <property name="shape" value="full"/>
   <property name="color" value="yellow"/>
  </properties>
 </tile>
 <tile id="163" type="region">
  <properties>
   <property name="shape" value="full"/>
   <property name="color" value="green"/>
  </properties>
 </tile>
 <tile id="164" type="region">
  <properties>
   <property name="shape" value="full"/>
   <property name="color" value="cyan"/>
  </properties>
 </tile>
 <tile id="165" type="region">
  <properties>
   <property name="shape" value="full"/>
   <property name="color" value="blue"/>
  </properties>
 </tile>
 <tile id="166" type="region">
  <properties>
   <property name="shape" value="full"/>
   <property name="color" value="purple"/>
  </properties>
 </tile>
 <tile id="167" type="region">
  <properties>
   <property name="shape" value="full"/>
   <property name="color" value="pink"/>
  </properties>
 </tile>
 <tile id="176" type="region">
  <properties>
   <property name="shape" value="ramp_sw"/>
  </properties>
 </tile>
 <tile id="192" type="region">
  <properties>
   <property name="shape" value="ramp_ne"/>
  </properties>
 </tile>
 <tile id="208" type="region">
  <properties>
   <property name="shape" value="ramp_se"/>
  </properties>
 </tile>
 <tile id="224" type="region">
  <properties>
   <property name="shape" value="ramp_nw"/>
  </properties>
 </tile>
</tileset>
