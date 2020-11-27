angular.module('styler-experiments', ['ngRoute', 'ui.bootstrap', 'anguFixedHeaderTable'])
	.config(function ($routeProvider, $locationProvider) {
		$routeProvider
			.when('/error/:project/:id', {
				controller: 'errorController'
			})
			.when('/', {
				controller: 'mainController'
			});
		// configure html5 to get links working on jsfiddle
		$locationProvider.html5Mode(false);
	})
	.directive('keypressEvents', [
		'$document',
		'$rootScope',
		function ($document, $rootScope) {
			return {
				restrict: 'A',
				link: function () {
					$document.bind('keydown', function (e) {
						$rootScope.$broadcast('keypress', e);
						$rootScope.$broadcast('keypress:' + e.which, e);
					});
				}
			};
		}
	])
	.directive('diff', ['$http', function ($http) {
		return {
			restrict: 'A',
			scope: {
				patch: '=diff'
			},
			link: function (scope, elem, attrs) {
				function printDiff(patch) {
					$(elem).text('')
					var diff = patch;
					if (diff != null) {
						var diff2htmlUi = new Diff2HtmlUI({ diff: diff });
						diff2htmlUi.draw($(elem), { inputFormat: 'java', showFiles: false, matching: 'none' });
						diff2htmlUi.highlightCode($(elem));
					}
				}
				scope.$watch('patch', function () {
					printDiff(scope.patch);
				})
				printDiff(scope.patch);
			}
		}
	}])
	.directive('error', ['$http', function ($http) {
		return {
			restrict: 'A',
			scope: {
				bug: '=error'
			},
			link: function (scope, elem, attrs) {
				function displayCode() {
					$(elem).html('<pre><code class="hljs java" data-ln-start-from="' + (scope.bug.information.errors[0].line - 2) + '">' + scope.bug.source_code + '</code></pre>')
					$('code.hljs').each(function (i, block) {
						hljs.highlightBlock(block);
						hljs.lineNumbersBlock(block);
					});
				}
				scope.$watch('bug', function () {
					displayCode()
				})
				displayCode()
			}
		}
	}])

	.controller('welcomeController', function ($uibModalInstance) {
		this.ok = function () {
			$uibModalInstance.close();
		};
	})
	.controller('errorModal', function ($rootScope, $uibModalInstance, error, classifications, $http) {
		var $ctrl = this;
		$ctrl.error = error;
		$ctrl.classifications = classifications;

		$rootScope.$on('new_error', function (e, error) {
			$ctrl.error = error;
			download();
		});
		$ctrl.ok = function () {
			$uibModalInstance.close();
		};
		$ctrl.nextError = function () {
			$rootScope.$emit('next_error', 'next');
		};
		$ctrl.previousError = function () {
			$rootScope.$emit('previous_error', 'previous');
		};

		var getName = function (type, key) {
			for (var group in $ctrl.classifications[type]) {
				if ($ctrl.classifications[type][group][key]) {
					if ($ctrl.classifications[type][group][key].fullname) {
						return $ctrl.classifications[type][group][key].fullname;
					} else {
						return $ctrl.classifications[type][group][key].name;
					}
				}
			}
			return null;
		}

		function download() {
			$http.get('data/' + $ctrl.error['project_name'] + '-' + $ctrl.error['error_id'] + '.json').then(function (response) {
				$ctrl.info = response.data;
			})
		}

		download();
	})
	.controller('errorController', function ($scope, $location, $rootScope, $routeParams, $uibModal) {
		var $ctrl = $scope;
		$ctrl.classifications = $scope.$parent.classifications;
		$ctrl.errors = $scope.$parent.filteredError;
		$ctrl.index = -1;
		$ctrl.error = null;

		$scope.$watch("$parent.filteredError", function () {
			$ctrl.errors = $scope.$parent.filteredError;
			$ctrl.index = getIndex($routeParams.project, $routeParams.id);
		});
		$scope.$watch("$parent.classifications", function () {
			$ctrl.classifications = $scope.$parent.classifications;
		});

		var getIndex = function (project, error_id) {
			if ($ctrl.errors == null) {
				return -1;
			}
			for (var i = 0; i < $ctrl.errors.length; i++) {
				if ($ctrl.errors[i].project_name == project && $ctrl.errors[i].error_id == error_id) {
					return i;
				}
			}
			return -1;
		};

		$scope.$on('$routeChangeStart', function (next, current) {
			$ctrl.index = getIndex(current.params.project, current.params.id);
		});

		var modalInstance = null;
		$scope.$watch("index", function () {
			if ($scope.index != -1) {
				if (welcomeModal != null) {
					welcomeModal.close();
				}
				if (modalInstance == null) {
					modalInstance = $uibModal.open({
						animation: true,
						ariaLabelledBy: 'modal-title',
						ariaDescribedBy: 'modal-body',
						templateUrl: 'modelError.html',
						controller: 'errorModal',
						controllerAs: '$ctrl',
						size: "lg",
						resolve: {
							error: function () {
								return $scope.errors[$scope.index];
							},
							classifications: $scope.classifications
						}
					});
					modalInstance.result.then(function () {
						modalInstance = null;
						$location.path("/");
					}, function () {
						modalInstance = null;
						$location.path("/");
					})
				}
				$rootScope.$emit('new_error', $scope.errors[$scope.index]);
			}
		});
		var welcomeModal = null;
		$scope.openWelcome = function () {
			welcomeModal = $uibModal.open({
				animation: true,
				ariaLabelledBy: 'modal-title',
				ariaDescribedBy: 'modal-body',
				templateUrl: 'welcome.html',
				controller: 'welcomeController',
				controllerAs: '$ctrl',
				size: "lg"
			});
			welcomeModal.result.then(function () {
				welcomeModal = null;
			}, function () {
				welcomeModal = null;
			})
		};

		var nextError = function () {
			console.log($scope)
			var index = $scope.index + 1;
			if (index == $ctrl.errors.length) {
				index = 0;
			}
			console.log(index, $ctrl.errors)

			$location.path("/error/" + $ctrl.errors[index].project_name + "/" + $ctrl.errors[index].error_id);
			if (gtag) {
				gtag('event', 'next', {
					'event_category': 'Shortcut'
				});
			}
			return false;
		};
		var previousError = function () {
			var index = $scope.index - 1;
			if (index < 0) {
				index = $ctrl.errors.length - 1;
			}

			$location.path("/error/" + $ctrl.errors[index].project_name + "/" + $ctrl.errors[index].error_id);
			if (gtag) {
				gtag('event', 'previous', {
					'event_category': 'Shortcut'
				});
			}
			return false;
		};

		$scope.$on('keypress:39', function () {
			console.log('here')
			$scope.$apply(function () {
				nextError();
			});
		});
		$scope.$on('keypress:37', function () {
			$scope.$apply(function () {
				previousError();
			});
		});
		$rootScope.$on('next_error', nextError);
		$rootScope.$on('previous_error', previousError);
	})
	.controller('mainController', function ($scope, $rootScope, $location, $window, $rootScope, $http, $uibModal) {
		$scope.sortType = ['project_name', 'error_id']; // set the default sort type
		$scope.sortReverse = false;
		$scope.match = "all";
		$scope.filter = {};
		$scope.pageTitle = "Styler experiments";

		// create the list of sushi rolls 
		$scope.errors = [];
		$scope.classifications = [];

		$http.get("data/classification.json").then(function (response) {
			$scope.classifications = response.data;
		});

		$http.get("data/all.json").then(function (response) {
			$scope.errors = response.data;

			var projects = {};
			var nbProjects = 0;

			var exceptions = {};
			var nbExceptions = 0;

			var tools = {};
			var nbTools = 0;

			var not_fixed_by_tools = {};
			var not_fixed_by_nbTools = 0;

			for (var i = 0; i < $scope.errors.length; i++) {
				var project = $scope.errors[i].project_name;
				if (projects[project] == null) {
					projects[project] = {
						"name": project,
						"fullname": project
					}
					nbProjects++;
				}
				$scope.errors[i][project] = true;

				var exception = $scope.errors[i].error_type.substring($scope.errors[i].error_type.lastIndexOf(".") + 1);
				if (exceptions[exception] == null) {
					exceptions[exception] = {
						"name": exception
					}
					nbExceptions++;
				}
				$scope.errors[i][exception] = true;

				for (var j = 0; j < $scope.errors[i].repaired_by.length; j++) {
					var tool = $scope.errors[i].repaired_by[j];
					if (tools[tool] == null) {
						tools[tool] = {
							"name": tool
						}
						nbTools++;
					}
					$scope.errors[i][tool] = true;
				}

				for (var j = 0; j < $scope.errors[i].not_repaired_by.length; j++) {
					var tool = 'not ' + $scope.errors[i].not_repaired_by[j];
					if (not_fixed_by_tools[tool] == null) {
						not_fixed_by_tools[tool] = {
							"name": tool
						}
						not_fixed_by_nbTools++;
					}
					$scope.errors[i][tool] = true;
				}
			}

			var sorted = [];
			for (var key in projects) {
				sorted[sorted.length] = key;
			}
			sorted.sort();
			var tempDict = {};
			for (var i = 0; i < sorted.length; i++) {
				tempDict[sorted[i]] = projects[sorted[i]];
			}

			projectLabel = "Projects (" + nbProjects + ")";
			$scope.classifications["Errors"][projectLabel] = tempDict;

			var sorted = [];
			for (var key in exceptions) {
				sorted[sorted.length] = key;
			}
			sorted.sort();
			var tempDict = {};
			for (var i = 0; i < sorted.length; i++) {
				tempDict[sorted[i]] = exceptions[sorted[i]];
			}

			exceptionLabel = "Error types (" + nbExceptions + ")";
			$scope.classifications["Errors"][exceptionLabel] = tempDict;

			toolLabel = "Fixed by (" + nbTools + ")";
			$scope.classifications["Repair"][toolLabel] = tools;

			toolLabell = "Not fixed by (" + not_fixed_by_nbTools + ")";
			$scope.classifications["Repair"][toolLabell] = not_fixed_by_tools;

			var element = angular.element(document.querySelector('#menu'));
			var height = element[0].offsetHeight;

			angular.element(document.querySelector('#mainTable')).css('height', (height - 160) + 'px');
		});

		$scope.filterName = function (filterKey) {
			for (var j in $scope.classifications) {
				for (var i in $scope.classifications[j]) {
					if ($scope.classifications[j][i][filterKey] != null) {
						if ($scope.classifications[j][i][filterKey].fullname) {
							return $scope.classifications[j][i][filterKey].fullname;
						}
						return $scope.classifications[j][i][filterKey].name;
					}
				}
			}
			return filterKey;
		}

		$scope.openError = function (error) {
			$location.path("/error/" + error.project_name + "/" + error.error_id);
		};

		$scope.sort = function (sort) {
			if (sort == $scope.sortType) {
				$scope.sortReverse = !$scope.sortReverse;
			} else {
				$scope.sortType = sort;
				$scope.sortReverse = false;
			}
			return false;
		}

		$scope.countErrors = function (key, filter) {
			if (filter.count) {
				return filter.count;
			}
			var count = 0;
			for (var i = 0; i < $scope.errors.length; i++) {
				if ($scope.errors[i][key] === true) {
					count++;
				}
			}
			filter.count = count;
			return count;
		};

		$scope.clickFilter = function (vKey) {
			if (gtag) {
				gtag('event', vKey, {
					'event_category': 'Filter',
					'event_label': $scope.filterNamevKey
				});
			}
		}

		$scope.errorsFilter = function (error) {
			var allFalse = true;
			for (var i in $scope.filter) {
				if ($scope.filter[i] === true) {
					allFalse = false;
					break;
				}
			}
			if (allFalse) {
				return true;
			}

			for (var i in $scope.filter) {
				if ($scope.filter[i] === true) {
					if (error[i] === true) {
						if ($scope.match == "any") {
							return true;
						}
					} else if ($scope.match == "all") {
						return false;
					}
				}
			}
			if ($scope.match == "any") {
				return false;
			} else {
				return true;
			}
		};

		$rootScope.$on('new_error', function (e, error) {
			var title = "Dissection of " + error.project_name + " " + error.error_id;
			$scope.pageTitle = title;

			if ($window.gtag) {
				$window.gtag('config', 'UA-5954162-27', { 'page_path': $location.path(), 'page_title': title });
			}
		});
	});
