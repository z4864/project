<?php
   $host='localhost';
   $uname='root';
   $pwd='wlqrkrhtlvek';
   $db='project';

   $con = mysql_connect($host,$uname,$pwd) or die("connection failed");
   
   mysql_select_db($db,$con) or die("db selection failed");
   
   //DB접속
	
   mysql_query("set names utf8");   

   $plate = $_POST[car];   

   $sql = "DELETE FROM `kpuparking_entrance` WHERE car_number_plate = '$plate'";
   $result = mysql_query($sql);


   $sql_plus = "UPDATE parking_info SET now=now+1 WHERE parking_name='kpuparking'";
   $result_plus = mysql_query($sql_plus);
 
   mysql_close($con);
?>