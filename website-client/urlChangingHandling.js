/**
 * Created by saharmehrpour on 8/1/17.
 */

/**
 * @param ruleTableManager
 * @param individualRuleManager
 * @param tagInformationManager
 * @constructor
 */
function UrlChangingHandling(ruleTableManager, individualRuleManager, tagInformationManager) {

    this.history = [];
    this.clicked = false;
    this.activeHash = -1;

    this.ruleTableManager = ruleTableManager;
    this.individualRuleManager = individualRuleManager;
    this.tagInformationManager = tagInformationManager;

    this.historyManager();

}

/**
 * This class updates the view based on changes in Hash address
 * @param hash
 */
UrlChangingHandling.prototype.hashManager = function (hash) {

    let self = this;

    self.updateHistory(hash);

    document.body.scrollTop = 0;
    document.documentElement.scrollTop = 0;

    d3.selectAll(".main").classed("hidden", true);
    d3.select("#tagInfo").classed("hidden", true); // TODO edit this

    let splittedHash = hash.split("/");

    d3.selectAll(".main").classed("hidden", true);
    d3.select("#header_2").classed("hidden", true);

    switch (splittedHash[1]) {
        case 'index':
            d3.select("#tableOfContent").classed("hidden", false);
            break;

        case 'tag':
            d3.select("#header_2").classed("hidden", false);
            d3.select("#ruleResults").classed("hidden", false);
            d3.select("#tagInfo").classed("hidden", false);

            this.tagInformationManager.displayTagInformation(splittedHash[2].split('+'));
            this.ruleTableManager.filterByTag(splittedHash[2].split('+'));
            break;

        case 'ruleGenerating':
            d3.select("#ruleGeneration").classed("hidden", false);
            break;

        case 'rules':
            d3.select("#page_title").text("All Rules");
            d3.select("#header_2").classed("hidden", false);
            this.ruleTableManager.cleanRuleTable();
            d3.select("#ruleResults").classed("hidden", false);
            break;

        case 'codeChanged':
            d3.select("#header_2").classed("hidden", false);
            d3.select("#ruleResults").classed("hidden", false);
            break;

        case 'rule':
            d3.select("#header_2").classed("hidden", false);
            d3.select("#individualRule").classed("hidden", false);
            this.individualRuleManager.displayRule(+splittedHash[2]);
            break;

    }

};

/**
 * adding listeners to tabs on the nav bar
 */
UrlChangingHandling.prototype.historyManager = function () {
    let self = this;

    d3.select("#back_button").on("click", () => {
        if (self.activeHash > 0) {
            self.activeHash = self.activeHash - 1;
            self.clicked = true;

            window.location.hash = self.history[self.activeHash];
            d3.select(d3.select('#forward_button').node().parentNode).classed('disabled', false);
        }
        if (self.activeHash === 0) {
            d3.select(d3.select('#back_button').node().parentNode).classed('disabled', true);
        }
    });

    d3.select("#forward_button").on("click", () => {
        if (self.activeHash < self.history.length - 1) {
            self.activeHash = self.activeHash + 1;
            self.clicked = true;

            window.location.hash = self.history[self.activeHash];
            d3.select(d3.select('#back_button').node().parentNode).classed('disabled', false);
        }
        if (self.activeHash === self.history.length - 1) {
            d3.select(d3.select('#forward_button').node().parentNode).classed('disabled', true);
        }
    });
};


/**
 * up date the hash list and 'active hash'
 * @param hash
 */
UrlChangingHandling.prototype.updateHistory = function (hash) {
    let self = this;

    if (!self.clicked) {
        if (self.history.length - 1 > self.activeHash) {
            for (let i = self.history.length - 1; i > self.activeHash; i--)
                console.log(self.history.pop());
        }
        self.history.push(hash);
        self.activeHash += 1;
        d3.select(d3.select('#back_button').node().parentNode).classed('disabled', self.activeHash === 0);
        d3.select(d3.select('#forward_button').node().parentNode).classed('disabled', true);
    }
    self.clicked = false;

};
