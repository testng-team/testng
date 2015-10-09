//****************************************************************
// Writes a table data element to the document. If pHRef equals
// pCurrentPage then 'class="current"' is added to the td element.
// For example:
//    writeTD("a.html", "a.html", "b")
// Would write
//    <td class="current"><a href="a.html">b</a><td>
//
// @param pCurrentPage the current page. For example "index.html"
// @param pHRef the string that should apear in the href
// @param pValue the string that should apear as the value
//****************************************************************
function writeTD(pCurrentPage, pHRef, pValue)
{
   document.write('                <td')
   document.write(pCurrentPage == pHRef ? ' class="current"' : '')
   document.write('><a href="')
   document.write(pHRef)
   document.write('">')
   document.write(pValue)
   document.writeln('</a></td>')
}

//******************************************************************
// Writes the main menu to the document.
// @param pCurrentPage the current page. For example "index.html"
//******************************************************************
function displayMenu(pCurrentPage) {
   document.writeln('<div id="topmenu">')
   document.writeln('    <table width="100%">')
   document.writeln('            <tr>')
       writeTD(pCurrentPage,             "index.html", "Welcome")
       writeTD(pCurrentPage,          "download.html", "Download")
       writeTD(pCurrentPage,"documentation-main.html", "Documentation")
       writeTD(pCurrentPage,         "migrating.html", "Migrating from JUnit")
       writeTD(pCurrentPage, "../javadocs/index.html", "JavaDoc")
       writeTD(pCurrentPage, "selenium.html", "Selenium")
   document.writeln('            </tr>')
   document.writeln('            <tr>')
       writeTD(pCurrentPage,           "eclipse.html", "Eclipse")
       writeTD(pCurrentPage,              "idea.html", "IDEA")
       writeTD(pCurrentPage,             "maven.html", "Maven")
       writeTD(pCurrentPage,               "ant.html", "Ant")
       writeTD(pCurrentPage,              "misc.html", "Miscellaneous")
       writeTD(pCurrentPage,              "book.html", "Book")
       writeTD(pCurrentPage,              "http://beust.com/kobalt", "Kobalt")
   document.writeln('            </tr>')
   document.writeln('        </table>')
   document.writeln('    </div>')

}

