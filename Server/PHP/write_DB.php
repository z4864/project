<?php
   $host='localhost';
   $uname='root';
   $pwd='wlqrkrhtlvek';
   $db="project";

   $con = mysql_connect($host,$uname,$pwd) or die("connection failed");
   
   mysql_select_db($db,$con) or die("db selection failed");
   
   //DB접속
	
   mysql_query("set names utf8");   

   $num = $_POST[number];
   $plate = $_POST[car];

   $sql = "UPDATE kpuparking SET car_number_plate='$plate' WHERE number='$num'";
   $result = mysql_query($sql);
 
   mysql_close($con);
?>