Ext.BLANK_IMAGE_URL = 'js/ext/images/default/s.gif';

var tree;
var testng = Sarissa.getDomDocument();
var dataStore;
var grid;
var toolbar;
var rootXPath = '';

Ext.onReady(function() {
  Ext.QuickTips.init();

  toolbar = new Ext.Toolbar('toolbar');
  //item.on('click', onItemClick);

  toolbar.add({
    id: 'show-passed',
    icon: 'images/show-passed.png', // icons can also be specified inline
    cls: 'x-btn-icon',
    tooltip: '<b>Show passed tests</b><br/>When selected, passed tests are shown in the tree',
    enableToggle: true,
    toggleHandler: onShowTestsToggle,
    pressed: true
  }, '-');

  toolbar.add({
    id: 'goto-error',
    icon: 'images/gotoerror.png',
    cls: 'x-btn-icon',
    disabled: true,
    tooltip: '<b>Go to fail</b><br/>Go to next failed test',
    handler: function() {
      if(!selectNextFailingTest()) {
        Ext.testng.msg("No more fails", "No more failed tests found in this test run");
      }
    }
  });

  toolbar.add({
    id: 'expand-all',
    icon: 'images/open_all.png',
    cls: 'x-btn-icon',
    tooltip: '<b>Open all</b><br/>Expand all visible tree nodes',
    handler: function() {
      tree.expandAll();
    }
  });

  toolbar.add({
    id: 'collapse-all',
    icon: 'images/close_all.png',
    cls: 'x-btn-icon',
    tooltip: '<b>Close all</b><br/>Collapse all visible tree nodes',
    handler: function() {
      tree.collapseAll();
    }
  });

  toolbar.add('-', {
    id: 'table-show-all',
    icon: 'images/fulltable.png', // icons can also be specified inline
    cls: 'x-btn-icon',
    tooltip: '<b>View All</b><br/>Show all nodes in table view',
    handler: function() {
      tree.getSelectionModel().clearSelections();
      showAllTestsInTable();
    }
  });

  toolbar.add('-', {
    id: 'show-tngxml',
    icon: 'images/show-tng.png', // icons can also be specified inline
    cls: 'x-btn-icon',
    tooltip: '<b>View testng.xml</b><br/>Show the testng.xml configuration file used for this run'
  });

  function onShowTestsToggle(item, pressed) {
    Ext.testng.msg(pressed ? 'Show tests' : 'Hide tests', 'Passed tests will now be ' + (pressed ? ' shown' : ' hidden'));
    loadTree(pressed);
    showAllTestsInTable();
  }
});

var ShowXML = function() {
  var dialog, showBtn;
  return {
    init : function() {
      showBtn = Ext.get('show-tngxml');
      showBtn.on('click', this.showDialog, this);
      dialog = new Ext.BasicDialog("xml-dlg", {
        autoTabs:true,
        width:550,
        height:400,
        shadow:true,
        proxyDrag: true
      });
      dialog.addKeyListener(27, dialog.hide, dialog);
      dialog.addButton('Close', dialog.hide, dialog);
    },

    showDialog : function() {
      dialog.show(showBtn.dom);
    }
  };
}();

var ShowStackTrace = function() {
  var dialog;
  return {
    init: function() {
      dialog = new Ext.BasicDialog("st-dlg", {
        width:600,
        height:400,
        shadow:true,
        proxyDrag: true,
        autoScroll: true
      });
      dialog.addKeyListener(27, dialog.hide, dialog);
      dialog.addButton('Close', dialog.hide, dialog);      
    },
    showDialog : function(text, el) {
      Ext.get('st-details').dom.value = text;
      dialog.show(el);
    }
  };
}();

Ext.onReady(ShowXML.init, ShowXML, true);
Ext.onReady(ShowStackTrace.init, ShowStackTrace, true);

Ext.testng = function() {
  var msgCt;

  function createBox(t, s) {
    return ['<div class="msg">',
    '<div class="x-box-tl"><div class="x-box-tr"><div class="x-box-tc"></div></div></div>',
    '<div class="x-box-ml"><div class="x-box-mr"><div class="x-box-mc"><h3>', t, '</h3>', s, '</div></div></div>',
    '<div class="x-box-bl"><div class="x-box-br"><div class="x-box-bc"></div></div></div>',
    '</div>'].join('');
  }

    return {
    msg : function(title, format) {
      if(!msgCt) {
        msgCt = Ext.DomHelper.insertFirst(document.body, {id:'msg-div'}, true);
      }
      msgCt.alignTo(document, 't-t');
      var s = String.format.apply(String, Array.prototype.slice.call(arguments, 1));
      var m = Ext.DomHelper.append(msgCt, {html:createBox(title, s)}, true);
      m.slideIn('t').pause(1).ghost("t", {remove:true});
    }
  };
}();

Ext.testng.reports = getQueryVariable('rd');
if(!Ext.testng.reports) Ext.testng.reports = '../test-output/xml'

Ext.testng.ClassStatusHolder = function() {
  this.passCount = 0;
  this.failCount = 0;
  this.skipCount = 0;
  this.status = "PASS";
};

function getQueryVariable(variable) {
  var query = window.location.search.substring(1);
  var vars = query.split("&");
  for(var i = 0; i < vars.length; i++) {
    var pair = vars[i].split("=");
    if(pair[0] == variable) {
      return pair[1];
    }
  }
  return null;
}

function updateHeader() {
  var q = Ext.DomQuery;
  var suite = q.select(rootXPath + 'suite', testng)[0];
  var suiteName = q.selectValue('@name', suite);
  Ext.get('suitename').dom.innerHTML = suiteName;
  var v = Ext.get('tngxml');
  if(rootXPath == '') suiteName = '../' + suiteName;
  v.dom.value = loadFile(suiteName + '-testng.xml');
}

function selectNextFailingTest() {
  var selected = tree.getSelectionModel().getSelectedNode();
  var selectedIndex = selected ? selected.attributes.index : 0;
  var tests = new Ext.util.MixedCollection();
  tests.addAll(tree.getRootNode().childNodes);
  var classes = new Ext.util.MixedCollection();
  var filterTests = tests.filterBy(function(item) {
    return item.attributes.status == 'FAIL' && item.attributes.index >= selectedIndex;
  }).each(function(item) {
    classes.addAll(item.childNodes);
  })
  for(var i = 0; i < classes.length; i++) {
    var cls = classes.get(i);
    if(cls.attributes.status = 'FAIL' && cls.attributes.index > selectedIndex) {
      cls.ensureVisible();
      cls.select();
      syncTableWithNode(cls);
      return true;
    }
  }
  return false;
}

function treeInit() {
  tree = new Ext.tree.TreePanel("tree", {animate:true, containerScroll: true, rootVisible: false});
  tree.on('click', function(node) {
    syncTableWithNode(node);
  });
  var root = new Ext.tree.TreeNode({text: '', expanded: true});
  tree.setRootNode(root);
  loadTree(true, true);
}

function loadTree(showPassed, updateCounts) {
  var q = Ext.DomQuery;
  var root = tree.getRootNode();
  if(root) {
    while(root.firstChild) {
      root.removeChild(root.firstChild);
    }
  }
  var tests = q.select(rootXPath + 'suite/test', testng);
  var hasFails = false;
  var index = 1;
  var testIndex;
  
  var passCount = 0;
  var failCount = 0;
  var skipCount = 0;
  
  var classStatus = new Ext.testng.ClassStatusHolder();
  
  var passedTests = new Array();
  var skippedTests = new Array();
  var failedTests = new Array();
  for(var i = 0; i < tests.length; i++) {
    testIndex = index++;
    var testName = q.selectValue('@name', tests[i]);
    var children = new Array();
    var expand = false;
    var testStatus = 'PASS';
    var classes = q.select('class', tests[i]);
    for(var j = 0; j < classes.length; j++) {
      inspectClass(classes[j], classStatus);
      if(updateCounts) {
        passCount += classStatus.passCount;
        failCount += classStatus.failCount;
        skipCount += classStatus.skipCount;
      }
      var icon = 'images/' + classStatus.status + '.png';
      if(showPassed || classStatus.status != 'PASS') {
        var className = q.selectValue('@name', classes[j]);
        var child = new Ext.tree.TreeNode({id: className, text: className, type:'class',
          status: classStatus.status, test: testName, icon: icon, index: index++});
        children[children.length] = child;
        if(classStatus.status != 'PASS') {
          //expand non-passes
          if(!expand)
            expand = true;
          //update test status, if it's not fail then check if we need to
          //escalate it to fail or skip
          if(testStatus != 'FAIL') {
            if(classStatus.status != 'PASS') {
              if(classStatus.status == 'FAIL') {
                hasFails = true;
                testStatus = 'FAIL';
              } else {
                testStatus = 'SKIP';
              }
            }
          }
        }
      }
    }
    var test = new Ext.tree.TreeNode({id: testName, text: testName, type:'test',
      status: testStatus, expanded : expand, icon: 'images/' + testStatus + '.png', index: testIndex});
    test.appendChild(children);
    if(showPassed || (test.attributes.status != 'PASS')) {
      switch(test.attributes.status) {
        case 'PASS':
          passedTests[passedTests.length] = test;
          break;
        case 'SKIP':
          skippedTests[skippedTests.length] = test;
          break;
        case 'FAIL':
          failedTests[failedTests.length] = test;
          break;
      }
    }
  }
  
  root.appendChild(failedTests);
  root.appendChild(skippedTests);
  root.appendChild(passedTests);
  
  if(updateCounts) {
    Ext.get('passcount').dom.innerHTML = passCount;
    Ext.get('skipcount').dom.innerHTML = skipCount;
    Ext.get('failcount').dom.innerHTML = failCount;
    var total = passCount + failCount + skipCount;
    Ext.get('totalcount').dom.innerHTML = total;
    var passPercent = (passCount / total) * 100;
    Ext.get('percent').dom.innerHTML = (Math.round(passPercent*10)/10) + '%';
    var passWidth = 200 * (passCount / total);
    Ext.get('pass-bar').setStyle("width", passWidth + 'px');
    Ext.get('fail-bar').setStyle("width", (200 - passWidth) + 'px');
  }
  tree.render();
  if(hasFails) {
    toolbar.items.get('goto-error').enable();
  }
}

function showAllTestsInTable() {
  var showPass = toolbar.items.get('show-passed').pressed ? "" : "[@status != 'PASS']";
  var xpath = rootXPath + 'suite/test/class/test-method';
  dataStore.reader = createReader(xpath + showPass);
  dataStore.load();
}

function syncTableWithNode(node) {
  if(node) {
    var showPass = toolbar.items.get('show-passed').pressed ? "" : "[@status != 'PASS']";
    var xpath;
    if(node.attributes.type == 'test')
      xpath = rootXPath + "suite/test[@name='" + node.id + "']/class/test-method"; 
    else if(node.attributes.type == 'class')
      xpath = rootXPath + "suite/test[@name='" + node.attributes.test + "]/class[@name='" + node.id + "']/test-method";
    dataStore.reader = createReader(xpath + showPass);
    dataStore.load();
  }
}

function inspectClass(cls, statusHolder) {
  statusHolder.passCount = 0;
  statusHolder.failCount = 0;
  statusHolder.skipCount = 0;
  var q = Ext.DomQuery;
  var status = q.select('test-method', cls);
  var ret = 'PASS';
  for(var i = 0; i < status.length; i++) {
    var s = q.selectValue('@status', status[i]);
    var currentStatus;
    switch(s) {
      case 'PASS':
        statusHolder.passCount++;
        break;
      case 'SKIP':
        statusHolder.skipCount++;
        if(ret == 'PASS')
          ret = 'SKIP';
        break;
      case 'FAIL':
        statusHolder.failCount++;
        ret = 'FAIL';
        break;
    }
  }
  statusHolder.status = ret;
}

function createReader(xpath) {
  return new Ext.data.XmlReader({record: xpath}, [
//  {name: 'cls', mapping: '@class'},
  {name: 'name', mapping: '@name'},
  {name: 'status', mapping: '@status', sortType: function(v) {
      switch(v)
      {
        case 'FAIL': return 1;
        case 'SKIP': return 0;
        case 'PASS': return -1;
      }
    }
  },
  {name: 'started-at', mapping: '@started-at', dateFormat: 'Y-m-d\\TG:i:s\\Z', type: "date"},
  {name: 'duration-ms', mapping: '@duration-ms', type: "int"},
  {name: 'description', mapping: 'exception/message'}
  ]);
}

function tableInit() {
  dataStore = new Ext.data.Store({
    proxy: new Ext.data.MemoryProxy(testng),
    reader: createReader(rootXPath + 'suite/test/class/test-method')
//    reader: createReader(rootXPath + '/suite/test/class/test-method[@status != "PASS"]')
  });
  dataStore.setDefaultSort('status', "DESC");
  
  var colModel = new Ext.grid.ColumnModel([
//  {header: "Class", width: 150, sortable: true, dataIndex: 'cls'},
  {header: "Method", width: 190, sortable: true, dataIndex: 'name'},
  {header: "Status", id:'status', width: 55, sortable: true, dataIndex: 'status', renderer: function(val, p, record) {
//    record.css = 'table_'
    return "<div class='table_" + val + "'>" + val + "</div>";
  }},
  {header: "Started", width: 60, sortable: true, dataIndex: 'started-at', renderer: function(val) {
    return val.format("G:i:s");
  }},
  {header: "Duration(ms)", width: 88, sortable: true, dataIndex: 'duration-ms'},
  {header: "Description", id:"description", width: 270, sortable: true, dataIndex: 'description', renderer: function(val, p, record) {
    if(val && val != '') {
      var q = Ext.DomQuery;
      var st = q.selectValue('exception/full-stacktrace', record.node);
      st = st.replace(/\n/g, '\\n');
      return '<a onclick="ShowStackTrace.showDialog(\'' + st + '\', this)">' + val + '</a>';
    } else {
      return val;
    }
  }}
  ]);

  grid = new Ext.grid.Grid('method-details', {
    ds: dataStore,
    cm: colModel,
    autoSizeColumns: false,
    autoExpandColumn: 'description',
    maxRowsToMeasure: 1,
    loadMask : {msg: 'Loading Data...'}
  });
  grid.getSelectionModel().lock();
  dataStore.load();
  grid.render();
}

function loadFile(name) {
  var xmlhttp = new XMLHttpRequest();
  xmlhttp.open("GET", Ext.testng.reports + '/' + name.replace(/ /g, '+'), false);
  xmlhttp.send('');
  return xmlhttp.responseText;
}

function layout() {
  var layout = new Ext.BorderLayout(document.body, {
    north: {
      split:false
    },
    west: {
      split:true,
      initialSize: 320,
      titlebar: true,
      collapsible: true,
      autoScroll: true
    },
    center: {
      autoScroll: false
    },
    south: {
      split:false
    }
  });
  var layout2 = new Ext.BorderLayout(Ext.get('nav'), {north: {split:false}, center:{autoScroll:true}});
  layout2.add('north', new Ext.ContentPanel('toolbar'));
  layout2.add('center', new Ext.ContentPanel('tree'));
  layout.beginUpdate();
  layout.add('north', new Ext.ContentPanel('header', {fitToFrame:true}));
  layout.add('west', new Ext.NestedLayoutPanel(layout2, {title: 'Navigation', fitToFrame:true}));
  //  layout.add('west', new Ext.ContentPanel('nav', {title: 'Navigation', fitToFrame:true}));
  layout.add('center', new Ext.GridPanel(grid));
  layout.add('south', new Ext.ContentPanel('footer'), {fitToFrame:true});
  layout.endUpdate();
}

Ext.onReady(function() {
  testng = (new DOMParser()).parseFromString(loadFile('testng-results.xml'), "text/xml");
  if(Ext.query('testng-results', testng).length > 0) {
    rootXPath = 'testng-results/';
  }
  updateHeader();
  treeInit();
  tableInit();
  layout();
  Ext.get('header').setVisible(true);
  Ext.get('footer').setVisible(true);
  Ext.fly('loading').remove();
});
