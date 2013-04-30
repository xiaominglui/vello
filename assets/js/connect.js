jQuery(function($) {

	var closeCurrentTab = function() {
		
	}, onAuthorize = function() {
		$("body").addClass("authorized");
		setTimeout(closeCurrentTab, 10000);
	};
						  
	Trello.authorize({
		interactive: false,
		success: onAuthorize
	});

	if (!Trello.authorized()) {
		Trello.authorize({
			expiration: "never",
			name: "vello",
			success: onAuthorize,
			scope: {
            read: true,
            write: true,
            account: true
          	}
		});
	}
});