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
   $time = $_POST[time];

   $sql = "INSERT INTO kpuparking_entrance values('$plate', '$time')";
   $result = mysql_query($sql);


   $sql_minus = "UPDATE parking_info SET now=now-1 WHERE parking_name='kpuparking'";
   $result_minus = mysql_query($sql_minus);
 
   mysql_close($con);
?>