const request = require('request-promise-native');

/**
 * Submit application
 *
 * @param {Object} options
 * @param {string} userToken User token
 * @param {Object} body Session data
 *
 * @returns {Promise} Request promise as returned by request-promise-native
 */
const submit = (options = {}, userToken = '', body = {}) => {
  const uri = `${options.baseUrl}/submit`;
  const headers = { Authorization: `Bearer ${userToken}` };

  return request.post({ uri, body, headers, json: true });
};

/**
 * Update application
 *
 * @param {Object} options
 * @param {string} userToken User token
 * @param {string} caseId Case ID returned from CCD on initial submission
 * @param {Object} eventData Session fields to update the application with
 * @param {string} eventId CCD event type
 *
 * @returns {Promise} Request promise as returned by request-promise-native
 */
const update = (options = {}, userToken = '', caseId = '', eventData = '', eventId = {}) => {
  const uri = `${options.baseUrl}/updateCase/${caseId}`;
  const body = { eventData, eventId };
  const headers = { Authorization: `Bearer ${userToken}` };

  return request.post({ uri, body, headers, json: true });
};


const restoreFromDraftStore = (options = {}, userToken = '') => {
  const uri = `${options.draftBaseUrl}`;
  const headers = { Authorization: `Bearer ${userToken}` };

  return request.get({ uri, headers, json: true });
};

const removeFromDraftStore = (options = {}, userToken = '') => {
  const uri = `${options.draftBaseUrl}`;
  const headers = { Authorization: `Bearer ${userToken}` };

  return request.delete({ uri, headers, json: true });
};

const saveToDraftStore = (options = {}, userToken = '', body = {}, sendEmail = false) => {
  let uri = `${options.draftBaseUrl}`;

  if (sendEmail && body.petitionerEmail) {
    uri += '?sendEmail=true';
  }

  const headers = { Authorization: `Bearer ${userToken}` };

  return request.put({ uri, body, headers, json: true });
};

const client = {
  /**
   * Create a transformation client
   *
   * @param {Object} options Options set with the following properties:
   *  - baseUrl: Base URL of the transformation service
   *
   * @returns {{submit: (function(...[*])), update: (function(...[*]))}}
   */
  init: (options = {}) => {
    if (!options.baseUrl) {
      throw new Error('Base URL must be set');
    }

    return {
      submit: (...args) => {
        return submit(options, ...args);
      },

      update: (...args) => {
        return update(options, ...args);
      },

      saveToDraftStore: (...args) => {
        return saveToDraftStore(options, ...args);
      },

      restoreFromDraftStore: (...args) => {
        return restoreFromDraftStore(options, ...args);
      },

      removeFromDraftStore: (...args) => {
        return removeFromDraftStore(options, ...args);
      }
    };
  }
};

module.exports = client;
