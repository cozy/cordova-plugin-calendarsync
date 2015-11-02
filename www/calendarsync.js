var argscheck = require('cordova/argscheck'),
    exec = require('cordova/exec');


var successCB = function(callback) {
    return function(result) {
        callback(null, result);
    }
}

var calendarsync = {

    all: function(options, callback) {
        argscheck.checkArgs('of', 'calendarsync.all', arguments);
        exec(successCB(callback), callback, 'CalendarSync', 'all',
            [options.accountType, options.accountName]);

    },

    allEvents: function(options, callback) {
        argscheck.checkArgs('of', 'calendarsync.allEvents', arguments);
        exec(successCB(callback), callback, 'CalendarSync', 'allEvents',
            [options.accountType, options.accountName]);

    },


    dirtyEvents: function(options, callback) {
        argscheck.checkArgs('of', 'calendarsync.dirtyEvents', arguments);
        exec(successCB(callback), callback, 'CalendarSync', 'dirtyEvents',
            [options.accountType, options.accountName]);
    },


    eventBySyncId: function(syncId, callback) {
        argscheck.checkArgs('sf', 'calendarsync.eventBySyncId', arguments);
        exec(successCB(callback), callback, 'CalendarSync', 'eventBySyncId',
            [syncId]);
    },

    addEvent: function(event, options, callback) {
        argscheck.checkArgs('oof', 'calendarsync.addEvent', arguments);
        exec(successCB(callback), callback, 'CalendarSync', 'addEvent',
            [event, options.accountType, options.accountName]);
    },

    updateEvent: function(event, options, callback) {
        argscheck.checkArgs('oof', 'calendarsync.updateEvent', arguments);
        exec(successCB(callback), callback, 'CalendarSync', 'updateEvent',
            [event, options.accountType, options.accountName]);
    },

    undirtyEvent: function(event, options, callback) {
        argscheck.checkArgs('oof', 'calendarsync.undirtyEvent', arguments);
        exec(successCB(callback), callback, 'CalendarSync', 'undirtyEvent',
            [event, options.accountType, options.accountName]);
    },

    deleteEvent: function(event, options, callback) {
        argscheck.checkArgs('oof', 'calendarsync.deleteEvent', arguments);
        exec(successCB(callback), callback, 'CalendarSync', 'deleteEvent',
            [event, options.accountType, options.accountName]);
    },

    allCalendars: function(options, callback) {
        argscheck.checkArgs('of', 'calendarsync.allCalendars', arguments);
        exec(successCB(callback), callback, 'CalendarSync', 'allCalendars',
            [options.accountType, options.accountName]);
    },

    addCalendar: function(calendar, callback) {
        argscheck.checkArgs('of', 'calendarsync.addCalendar', arguments);
        exec(successCB(callback), callback, 'CalendarSync', 'addCalendar',
            [calendar, calendar.accountType, calendar.accountName]);
    },

    updateCalendar: function(calendar, options, callback) {
        argscheck.checkArgs('oof', 'calendarsync.updateCalendar', arguments);
        exec(successCB(callback), callback, 'CalendarSync', 'updateCalendar',
            [calendar, options.accountType, options.accountName]);
    },

    deleteCalendar: function(calendar, options, callback) {
        argscheck.checkArgs('oof', 'calendarsync.deleteCalendar', arguments);
        exec(successCB(callback), callback, 'CalendarSync', 'deleteCalendar',
            [calendar, options.accountType, options.accountName]);
    }


};

module.exports = calendarsync;
