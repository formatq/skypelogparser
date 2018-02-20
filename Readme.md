# The application prepares a csv file of logs Skype for easy import in Excel

The new versions of Skype (e.g. 7.40.76.202) have the opportunity to upload a story.


Build

`mvn clean package`

Start

`java -jar skypelogparser.jar C:\skypelog.csv`

The end result file is

`skypelog - handled.csv`


**Some comments**

In the new converted file removed column 'timespan' log, but added the following columns:
* Date (e.g. 22.11.2001).
* Date and Time (e.g. 22.11.2001 15:55).
* Total conversation time in minutes (e.g. <empty> or 5).  

The column splitter is the character `~`.

The first line contains the column headers.

The transformed csv file can be loaded into excel document using the data import wizard.
After importing, you can do data filtering and comfortable to manipulate the slices by date.


