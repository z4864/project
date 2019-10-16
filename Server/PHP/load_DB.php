<?php
   $host='localhost';
   $uname='root';
   $pwd='wlqrkrhtlvek';
   $db="project";

   $con = mysql_connect($host,$uname,$pwd) or die("connection failed");
   
   mysql_select_db($db,$con) or die("db selection failed");
   
   //DBÁ¢¼Ó
	
   mysql_query("set names utf8");   

   $result=mysql_query("select * from kpuparking",$con);
   $cnt=0;
   $arr=array();
   
   while($row=mysql_fetch_array($result)){
      
      $count=$cnt;
      $arr[$count]['NUMBER']=$row[0];    
      $arr[$count]['CAR_NUMBER_PLATE']=$row[1];
      $arr[$count]['X1']=$row[2];
      $arr[$count]['Y1']=$row[3];
      $arr[$count]['X2']=$row[4];
      $arr[$count]['Y2']=$row[5];
      $cnt++;
   }

   
   header('Content-Type: application/json; charset=utf8');
   $json = json_encode(array("ParkingArea"=>$arr), JSON_PRETTY_PRINT+JSON_UNESCAPED_UNICODE);
   print($json);
   mysql_close($con);
?>