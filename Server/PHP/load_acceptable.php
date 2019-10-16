<?php
   $host='localhost';
   $uname='root';
   $pwd='wlqrkrhtlvek';
   $db="project";

   $con = mysql_connect($host,$uname,$pwd) or die("connection failed");
   
   mysql_select_db($db,$con) or die("db selection failed");
   
   //DBÁ¢¼Ó
	
   mysql_query("set names utf8");   
   
   $parking = $_GET['PARKING'];

   $result=mysql_query("select * from parking_info where parking_name='$parking'",$con);
   $cnt=0;
   $arr=array();
   
   while($row=mysql_fetch_array($result)){
      
      $count=$cnt;
      $arr[$count]['parking_name']=$row[0];    
      $arr[$count]['now']=$row[1];
      $arr[$count]['fee']=$row[2];
      $cnt++;
   }

   header('Content-Type: application/json; charset=utf8');
   $json = json_encode(array("result"=>$arr), JSON_PRETTY_PRINT+JSON_UNESCAPED_UNICODE);
   print($json);
   mysql_close($con);
?>