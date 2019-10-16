#Main.py

import cv2
import numpy as np
import os

from PIL import Image
import pytesseract

from picamera.array import PiRGBArray
from picamera import PiCamera
import time
import threading
from multiprocessing import Process, Pipe
from datetime import datetime, timedelta

import DetectChars
import DetectPlates
import PossiblePlate
import requests

# module level variables ##########################################################################
SCALAR_BLACK = (0.0, 0.0, 0.0)
SCALAR_WHITE = (255.0, 255.0, 255.0)
SCALAR_YELLOW = (0.0, 255.0, 255.0)
SCALAR_GREEN = (0.0, 255.0, 0.0)
SCALAR_RED = (0.0, 0.0, 255.0)

showSteps = False


###################################################################################################
def main(num):
    IsFirst = True
    url = 'http://drgcream.ipdisk.co.kr:8000/apps/xe/write_DB_entrance.php'

    img_num = '%d.jpg' % num
    img = cv2.imread(img_num)
    ret, th3 = cv2.threshold(img, 100, 255, cv2.THRESH_BINARY)
    imgOriginalScene = th3
    cv2.imwrite("original.png", imgOriginalScene)

    if imgOriginalScene is None:  # if image was not read successfully
        print("\nerror: image not read from file \n\n")  # print error message to std out
        os.system("pause")  # pause so user can see error message
        return  # and exit program
    # end if

    listOfPossiblePlates = DetectPlates.detectPlatesInScene(imgOriginalScene)  # detect plates

    listOfPossiblePlates = DetectChars.detectCharsInPlates(listOfPossiblePlates)  # detect chars in plates

    # cv2.imshow("imgOriginalScene", imgOriginalScene)  # show scene image

    if len(listOfPossiblePlates) == 0:  # if no plates were found
        print("\nno license plates were detected\n")  # inform user no plates were found
        txt = '0'
    else:  
			# else
        # if we get in here list of possible plates has at leat one plate

        # sort the list of possible plates in DESCENDING order (most number of chars to least number of chars)
        listOfPossiblePlates.sort(key=lambda possiblePlate: len(possiblePlate.strChars), reverse=True)

        # suppose the plate with the most recognized chars (the first plate in sorted by string length descending order) is the actual plate
        licPlate = listOfPossiblePlates[0]

        cv2.imwrite("imgPlate.png", licPlate.imgPlate)
        cv2.imwrite("imgThresh.png", licPlate.imgThresh)

        if len(licPlate.strChars) == 0:  # if no chars were found in the plate
			print("\nno characters were detected\n\n")  # show message
			licPlate.strChars = '0'
        
        else:
			print("text = " + licPlate.strChars)
			txt = licPlate.strChars
			' '.join(txt.split())
        
			requests.post(url, data={'car': txt, 'time': datetime.now()}).text
			# end if

# cv2.imwrite("imgOriginalScene.png", imgOriginalScene)  # write image out to file

# end if else

# end main
