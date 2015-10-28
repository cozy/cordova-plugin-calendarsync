var argscheck = require('cordova/argscheck'),
    exec = require('cordova/exec');
    // // ContactError = require('./ContactError'),
    // utils = require('cordova/utils'),
    // // Contact = require('./Contact'),
    // fieldType = require('./ContactFieldType');


/**
* Represents a group of Contacts.
* @constructor
*/
var successCB = function(callback) {
    return function(result) {
        callback(null, result);
    }
}

var calendarsync = {
    // fieldType: fieldType,
    /**
     * Returns an array of Contacts matching the search criteria.
     * @param fields that should be searched
     * @param successCB success callback
     * @param errorCB error callback
     * @param {ContactFindOptions} options that can be applied to contact searching
     * @return array of Contacts matching search criteria
     */
    // find:function(fields, successCB, errorCB, options) {
    //     argscheck.checkArgs('afFO', 'contacts.find', arguments);
    //     if (!fields.length) {
    //         errorCB && errorCB(new ContactError(ContactError.INVALID_ARGUMENT_ERROR));
    //     } else {
    //         // missing 'options' param means return all contacts
    //         options = options || {filter: '', multiple: true}
    //         var win = function(result) {
    //             var cs = [];
    //             for (var i = 0, l = result.length; i < l; i++) {
    //                 cs.push(contacts.create(result[i]));
    //             }
    //             successCB(cs);
    //         };
    //         exec(win, errorCB, "Contacts", "search", [fields, options]);
    //     }
    // },

    /**
     * This function picks contact from phone using contact picker UI
     * @returns new Contact object
     */
    // pickContact: function (successCB, errorCB) {

    //     argscheck.checkArgs('fF', 'contacts.pick', arguments);

    //     var win = function (result) {
    //         // if Contacts.pickContact return instance of Contact object
    //         // don't create new Contact object, use current
    //         var contact = result instanceof Contact ? result : contacts.create(result);
    //         successCB(contact);
    //     };
    //     exec(win, errorCB, "Contacts", "pickContact", []);
    // },

    /**
     * This function creates a new contact, but it does not persist the contact
     * to device storage. To persist the contact to device storage, invoke
     * contact.save().
     * @param properties an object whose properties will be examined to create a new Contact
     * @returns new Contact object
     */
    // create: function(properties) {
    //     argscheck.checkArgs('O', 'contacts.create', arguments);
    //     var contact = new Contact();
    //     for (var i in properties) {
    //         if (typeof contact[i] !== 'undefined' && properties.hasOwnProperty(i)) {
    //             contact[i] = properties[i];
    //         }
    //     }
    //     return contact;
    // },


    createAccount: function(accountType, accountName, successCB, errorCB) {
        argscheck.checkArgs('ssfF', 'calendarsync.createAccount', arguments);
        exec(successCB, errorCB, 'CalendarSync', 'createAccount',
            [accountName, accountType]);

    },

    createCalendar: function(successCB, errorCB) {
        argscheck.checkArgs('fF', 'calendarsync.createCalendar', arguments);
        exec(successCB, errorCB, 'CalendarSync', 'createCalendar', []);

    },

    listAccounts: function(successCB, errorCB) {
        argscheck.checkArgs('fF', 'calendarsync.listAccounts', arguments);
        exec(successCB, errorCB, "CalendarSync", "listAccounts", []);
    },
    listDirty: function(successCB, errorCB) {
        argscheck.checkArgs('fF', 'calendarsync.listDirty', arguments);
        exec(successCB, errorCB, "CalendarSync", "listDirty", []);
    },
    all: function(accountType, accountName, successCB, errorCB) {
        argscheck.checkArgs('ssfF', 'calendarsync.all', arguments);
        exec(successCB, errorCB, 'CalendarSync', 'all',
            [accountType, accountName]);

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

    deleteCalendar: function(calendar, options, callback) {
        argscheck.checkArgs('oof', 'calendarsync.deleteCalendar', arguments);
        exec(successCB(callback), callback, 'CalendarSync', 'deleteCalendar',
            [calendar, options.accountType, options.accountName]);
    }


};

module.exports = calendarsync;
