// Code goes here

// Code goes here

// Code goes here

var app = angular.module('myApp', ['ui.router']);

app.config(function($stateProvider, $urlRouterProvider) {

  $urlRouterProvider.otherwise('/home');

  $stateProvider

  // HOME STATES AND NESTED VIEWS ========================================
    .state('home', {
    url: '/home',
    templateUrl: 'htmltemplates/auv.html',
    controller: 'MainController'

  })

  // ABOUT PAGE AND MULTIPLE NAMED VIEWS =================================
  .state('vct', {
    url: '/vct',
    templateUrl: 'htmltemplates/vct.html',
    controller: 'VCTController'
  });

});

app.controller('MainController', function($scope, $http, $location, $rootScope) {
    // initializing values
  $scope.submitVisible = true;
  $scope.authorizeVisible = false;
  $scope.approveVisible = false;
  $scope.reviseVisible = false;
  $scope.customerCommentsVisible = false;
  $scope.vctVisible = false;
  $scope.imageShow= false;
   $scope.showmessage= false
 // images path by default
  $scope.leftimageUrl = "image/Operationalcts.png";
  $scope.rightimageUrl = "image/TacticalCognizant.png";
  $scope.senderRole = "Operational Cognizant";
  $scope.receiverRole = "Tactical Cognizant";

  var onUserComplete = function(response) {
    $scope.showmessage= true
    $scope.data = response.data;
    $scope.message = "Submitted for authorization";
    var status = response.data.status;
    alert("Submitted for authorization");
    $scope.submitVisible = false;
    $scope.authorizeVisible = true;
    $scope.approveVisible = false;
    $scope.leftimageUrl = "image/TacticalCognizant.png";
    $scope.rightimageUrl = "image/Operationalcts.png";
    $scope.senderRole = "Tactical Cognizant";
    $scope.receiverRole = "Operational Cognizant";
    // display one more image
    $scope.imageShow= true;
    $scope.rightimageUrl1 = "image/Tacticalcust.jpg";
    $scope.receiverRole1= "Tactical Customer";
  };

  var onAuthorize = function(response) {
    $scope.showmessage= true
     $scope.customerCommentsVisible = true;
    $scope.data = response.data;
    $scope.message = "Submitted for Approval";
    alert("Submitted for Approval");
    $scope.submitVisible = false;
    $scope.authorizeVisible = false;
    $scope.approveVisible = true;
    $scope.leftimageUrl = "image/Tacticalcust.jpg";
    $scope.rightimageUrl = "image/TacticalCognizant.png";
    $scope.senderRole = "Tactical Customer";
    $scope.receiverRole = "Tactical Cognizant";
    // display one more image
        $scope.imageShow= true;
        $scope.rightimageUrl1 = "image/Operationalcts.png";
        $scope.receiverRole1= "Operational Cognizant";
        $scope.custcomment = false

  };

  var onApprove = function(response) {
    $scope.showmessage= true
    $scope.data = response.data;
    $scope.message = "Approved"
    alert("To Create a new VCT click the link below");
    $scope.submitVisible = false;
    $scope.authorizeVisible = false;
    $scope.approveVisible = false;
    $scope.vctVisible = true;
    $rootScope.serviceCreditId = response.data.serviceCreditId;
    $rootScope.attachmentHash = response.data.attachment;
    console.log($rootScope.serviceCreditId)
  };


  var onError = function(reason) {
  $scope.showmessage= true
    $scope.message = reason.data;

  };



  $scope.submit = function() {
    var request = {

      "businessUnit": $scope.businessUnit,

      "accountName": $scope.accountName,

      "projectName": $scope.projectName,

      "customerName": $scope.customerName,

      "lob": $scope.lob,

      "internalComments": $scope.internalComments,

      "serviceCredit": $scope.serviceCredit,

      "startYear": "2018-09-20",

      "endYear": "2020-09-20",

      "status": "Submitted",

      "attachmentPath": "kotlin-source/src/Sample.zip",

      "projectType": $scope.projectType

    }
    $http.post("http://13.71.113.123:10007/api/valueArticulation/create-serviceCredits", request)
      .then(onUserComplete, onError);
  }



  $scope.authorize = function() {
    $scope.showmessage= false
    console.log("Authorisation")

    var request = {

      "serviceCreditId": $scope.data.serviceCreditId,

      "businessUnit": $scope.businessUnit,

      "accountName": $scope.accountName,

      "projectName": $scope.projectName,

      "customerName": $scope.customerName,

      "lob": $scope.lob,

      "internalComments": $scope.internalComments,

      "serviceCredit": $scope.serviceCredit,

      "startYear": "2018-09-20",

      "endYear": "2020-09-20",

      "status": "Authorized",

      "attachmentPath": "kotlin-source/src/Sample.zip"

    }
    $http.post("http://13.71.113.123:10010/api/valueArticulation/authorize-serviceCredits", request)
      .then(onAuthorize, onError);
  }

  $scope.approve = function() {
$scope.showmessage= false
    $customerCommentsVisible = true;
    console.log("Approved")

    var request = {

      "serviceCreditId": $scope.data.serviceCreditId,

      "businessUnit": $scope.businessUnit,

      "accountName": $scope.accountName,

      "projectName": $scope.projectName,

      "customerName": $scope.customerName,

      "lob": $scope.lob,

      "internalComments": $scope.internalComments,

      "serviceCredit": $scope.serviceCredit,

      "startYear": "2018-09-20",

      "endYear": "2020-09-20",

      "status": "Approved",

      "attachmentPath": "kotlin-source/src/Sample.zip"

    }
    $http.post("http://13.71.113.123:10016/api/valueArticulation/approve-serviceCredits", request)
      .then(onApprove, onError);

  }


});

app.controller("VCTController", function($scope, $http, $rootScope) {

   $scope.hidecustomer= true;
  $scope.leftimageUrl = "image/Operationalcts.png";
  $scope.rightimageUrl = "image/OperationalCustomer.png";
  $scope.imageShow= false
  $scope.senderRole = "Operational Cognizant";
  $scope.receiverRole = "Operational Customer";
  $scope.submitVisible = true;
  $scope.authorizeVisible = false;
  $scope.approveVisible = false;
  $scope.reviseVisible = false;
  $scope.vctVisible = false;
   $scope.customerCommentsVisible = false;
    $scope.showmessage= false
  var onError = function(reason) {
    $scope.message = "There was a error in submitting";

  };


  $scope.executevct = function() {
      $scope.showmessage= false
    console.log("inside execute vct" + $rootScope.serviceCreditId);
    var request = {

      "serviceCreditId": $rootScope.serviceCreditId,

      "lob": $scope.lob,

      "leverCategory": $scope.leverCategory,

      "valueImprovementProgram": $scope.valueImprovementProgram,

      "valueCategory": $scope.valueCategory,

      "theme": $scope.theme,

      "valueAddDescription": $scope.valueAddDescription,

      "agreedServiceCredits": $scope.agreedServiceCredits,

      "implementationDate": "2018-09-20",

      "internalComments": $scope.internalComments,

      "customerComments": $scope.customerComments,

      "status": "Submitted",

      "transactionapproverName": "Steve"

    }
    $http.post("http://13.71.113.123:10007/api/valueArticulation/execute-ValueContractTransaction", request)
      .then(onExecuteVct, onError);
  }


  $scope.authorizevct = function() {

    $scope.showmessage= false
    console.log("Inside authorizevct" + $rootScope.serviceCreditId)

    var request = {

      "valueContractTransactionId": $scope.data.valueContractTransactionId,

      "serviceCreditId": $rootScope.serviceCreditId,

      "lob": $scope.lob,

      "leverCategory": $scope.leverCategory,

      "valueImprovementProgram": $scope.valueImprovementProgram,

      "valueCategory": $scope.valueCategory,

      "theme": $scope.theme,

      "valueAddDescription": $scope.valueAddDescription,

      "agreedServiceCredits": $scope.agreedServiceCredits,

      "implementationDate": "2018-09-20",

      "internalComments": $scope.internalComments,

      "customerComments": $scope.customerComments,

      "status": "Authorized",

      "transactionapproverName": "Steve Jobs"

    }
    $http.post("http://13.71.113.123:10010/api/valueArticulation/authorize-ValueContractTransaction", request)
      .then(onAuthorizeVct, onError);
  }


  $scope.approvevct = function() {

    $scope.showmessage= false
    console.log("Approve VCT id" + $scope.data.valueContractTransactionId)
    console.log("Inside Approvevct" + $rootScope.serviceCreditId)
    var request = {

      "valueContractTransactionId": $scope.data.valueContractTransactionId,

      "serviceCreditId": $rootScope.serviceCreditId,

      "lob": $scope.lob,

      "leverCategory": $scope.leverCategory,

      "valueImprovementProgram": $scope.valueImprovementProgram,

      "valueCategory": $scope.valueCategory,

      "theme": $scope.theme,

      "valueAddDescription": $scope.valueAddDescription,

      "agreedServiceCredits": $scope.agreedServiceCredits,

      "implementationDate": "2018-09-20",

      "internalComments": $scope.internalComments,

      "customerComments": $scope.customerComments,

      "status": "Approved",

      "transactionapproverName": "Steve Jobs"

    }
    $http.post("http://13.71.113.123:10013/api/valueArticulation/approve-ValueContractTransaction", request)
      .then(onApproveVct, onError);
  }


  $scope.revisevct = function() {
    $scope.imageShow= false
    $scope.showmessage= false
    console.log("Revised")

    var request = {

      "valueContractTransactionId": $scope.data.valueContractTransactionId,

      "serviceCreditId": $rootScope.serviceCreditId,

      "lob": $scope.lob,

      "leverCategory": $scope.leverCategory,

      "valueImprovementProgram": $scope.valueImprovementProgram,

      "valueCategory": $scope.valueCategory,

      "theme": $scope.theme,

      "valueAddDescription": $scope.valueAddDescription,

      "agreedServiceCredits": $scope.agreedServiceCredits,

      "implementationDate": "2018-09-20",

      "internalComments": $scope.internalComments,

      "customerComments": $scope.customerComments,

      "status": "Revised",

      "transactionapproverName": "Steve"


    }
    $http.post("http://13.71.113.123:10007/api/valueArticulation/revise-ValueContractTransaction", request)
      .then(onReviseVct, onError);

  }
  var onExecuteVct = function(response) {

    $scope.showmessage= true
    $scope.data = response.data;
    $scope.message = "Executed Value contract Transaction";
    console.log("Data inside Execute VCT==" + $scope.data);
    console.log("leverCategory==" + response.data.leverCategory);
    alert("Executed a Value Add");

    // check if it is a Value Retention
    if (response.data.valueCategory == "Retention") {
      $scope.hidecustomer= false;
      console.log("inside Retention")
      $scope.approveVisible = true;
      $scope.customerCommentsVisible = true;
      $scope.leftimageUrl = "image/OperationalCustomer.png";
      $scope.rightimageUrl = "image/Operationalcts.png";
      $scope.senderRole = "Operational Customer";
      $scope.receiverRole = "Operational Cognizant";
        // display one more image
                        $scope.imageShow= true;
                        $scope.rightimageUrl1 = "image/TacticalCognizant.png";
                        $scope.receiverRole1= "Tactical Cognizant";
    } else {
      console.log("not inside retention")
      $scope.authorizeVisible = true;
       $scope.leftimageUrl = "image/TacticalCognizant.png";
       $scope.rightimageUrl = "image/Tacticalcust.jpg";
       $scope.senderRole = "Tactical Cognizant";
      $scope.receiverRole = "Tactical Customer";

       // display one more image
                       $scope.imageShow= true;
                       $scope.imageShow= true;
                       $scope.rightimageUrl1 = "image/Operationalcts.png";
                       $scope.receiverRole1= "Operational Cognizant";

    }

    // set the visible button
    $scope.submitVisible = false;
    $scope.reviseVisible = false;
    $scope.vctVisible = false;
 };

  var onAuthorizeVct = function(response) {
  $scope.showmessage= true
   $scope.customerCommentsVisible = true;
    $scope.data = response.data;
    $scope.message = "Submitted for Approval";
    alert("Submitted for Authorization");
    $scope.submitVisible = false;
    $scope.authorizeVisible = false;
    $scope.reviseVisible = false;
    $scope.approveVisible = true;
    $scope.leftimageUrl = "image/OperationalCustomer.png";
    $scope.rightimageUrl = "image/Operationalcts.png";
    $scope.senderRole = "Operational Customer";
    $scope.receiverRole = "Operational Cognizant";
    $scope.hidecustomer= false;
    $scope.imageShow= true;
    // display one more image
                    $scope.imageShow= true;
                    $scope.rightimageUrl1 = "image/TacticalCognizant.png";
                    $scope.receiverRole1= "Tactical Cognizant";
   };

  $scope.createvct = function() {
  $scope.showmessage= false
    $scope.leftimageUrl = "image/Operationalcts.png";
      $scope.rightimageUrl = "image/OperationalCustomer.png";
       $scope.senderRole = "Operational Cognizant";
        $scope.receiverRole = "Operational Customer";
    $scope.imageShow= false
    $scope.submitVisible = true;
    $scope.authorizeVisible = false;
    $scope.approveVisible = false;
    $scope.reviseVisible = false;
    $scope.vctVisible = false;
    $('input[type=text],input[type=date').val('')
  }

  var onApproveVct = function(response) {
    $scope.showmessage= true
      $scope.hidecustomer= true;
    $scope.data = response.data;
    $scope.message = "Approved"
    alert("Approved");
    $scope.submitVisible = false;
    $scope.authorizeVisible = false;
    $scope.approveVisible = false;
    $scope.reviseVisible = true;
    $scope.vctVisible = true;
    $scope.leftimageUrl = "image/Operationalcts.png";
    $scope.rightimageUrl = "image/OperationalCustomer.png";
    $scope.senderRole = "Operational Cognizant";
    $scope.receiverRole = "Operational Customer";

  };


  var onReviseVct = function(response) {
    $scope.showmessage= true
    $scope.data = response.data;
    $scope.message = "Revised";
    alert("Revised");
    $scope.submitVisible = false;
    $scope.authorizeVisible = false;
    $scope.reviseVisible = false;
    $scope.vctVisible = false;
    if (response.data.valueCategory == "Retention") {
            $scope.imageShow= false;
           $scope.hidecustomer= false;
          console.log("inside Retention")
          $scope.approveVisible = true;
          $scope.leftimageUrl = "image/OperationalCustomer.png";
          $scope.rightimageUrl = "image/Operationalcts.png";
          $scope.senderRole = "Operational Customer";
          $scope.receiverRole = "Operational Cognizant";
          // display one more image
                          $scope.imageShow= true;
                          $scope.rightimageUrl1 = "image/TacticalCognizant.png";
                          $scope.receiverRole1= "Tactical Cognizant";

        } else {

          console.log("not inside retention")
          $scope.hidecustomer= true;
          $scope.authorizeVisible = true;
           $scope.leftimageUrl = "image/TacticalCognizant.png";
           $scope.rightimageUrl = "image/Tacticalcust.jpg";
            $scope.senderRole = "Tactical Cognizant";
            $scope.receiverRole = "Tactical Customer";
                  // display one more image
                                  $scope.imageShow= true;
                                  $scope.rightimageUrl1 = "image/Operationalcts.png";
                                  $scope.receiverRole1= "Operational Cognizant";

        }
    /*$scope.leftimageUrl = "image/Operationalcts.png";
    $scope.rightimageUrl = "image/OperationalCustomer.png";
    $scope.senderRole = "Tactical Cognizant";
    $scope.receiverRole = "Operational Customer";*/
  };
    var onError = function(reason) {
        $scope.message = reason.data;

      };
});