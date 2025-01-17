const request = require('supertest');
const server = require('app');
const {
  testContent, testErrors, testRedirect,
  testCYATemplate, testExistenceCYA
} = require('test/util/assertions');
const { withSession } = require('test/util/setup');
const idamMock = require('test/mocks/idam');
const { removeStaleData } = require('app/core/helpers/staleDataManager');
const { expect } = require('test/util/chai');
const { clone } = require('lodash');

const modulePath = 'app/steps/grounds-for-divorce/desertion/agree';

const content = require(`${modulePath}/content`);

let s = {};
let agent = {};
let underTest = {};

describe(modulePath, () => {
  beforeEach(() => {
    idamMock.stub();
    s = server.init();
    agent = request.agent(s.app);
    underTest = s.steps.DesertionAgree;
  });

  afterEach(() => {
    idamMock.restore();
  });

  describe('success', () => {
    let session = {};

    beforeEach(done => {
      session = { divorceWho: 'wife' };
      withSession(done, agent, session);
    });

    it('renders the content from the content file', done => {
      testContent(done, agent, underTest, content, session);
    });

    it('renders errors for missing required context', done => {
      const context = {};

      testErrors(done, agent, underTest, context, content, 'required', 'divorceWho', session);
    });

    it('redirects to the next page', done => {
      const context = { reasonForDivorceDesertionAgreed: 'No' };

      testRedirect(done, agent, underTest, context, s.steps.ExitDesertionAgree);
    });

    it('redirects to the exit page', done => {
      const context = { reasonForDivorceDesertionAgreed: 'Yes' };

      testRedirect(done, agent, underTest, context, s.steps.DesertionDate);
    });
  });

  describe('Watched session values', () => {
    it('removes reasonForDivorceDesertionAgreed fields if reasonForDivorce is changed', () => {
      const previousSession = {
        reasonForDivorceDesertionAgreed: true,
        reasonForDivorce: 'desertion'
      };

      const session = clone(previousSession);
      session.reasonForDivorce = 'anything';

      const newSession = removeStaleData(previousSession, session);

      expect(typeof newSession.reasonForDivorceDesertionAgreed)
        .to.equal('undefined');
    });

    it('do not remove reasonForDivorceDesertionAgreed fields if reasonForDivorce does not change', () => {
      const previousSession = {
        reasonForDivorceDesertionAgreed: true,
        reasonForDivorce: 'desertion'
      };

      const session = clone(previousSession);
      session.reasonForDivorce = 'desertion';

      const newSession = removeStaleData(previousSession, session);

      expect(newSession.reasonForDivorceDesertionAgreed)
        .to.equal(previousSession.reasonForDivorceDesertionAgreed);
    });
  });

  describe('Check Your Answers', () => {
    it('renders the cya template', done => {
      testCYATemplate(done, underTest);
    });

    it('renders desertion agreement', done => {
      const contentToExist = [ 'question' ];

      const valuesToExist = [
        'reasonForDivorceDesertionAgreed',
        'divorceWho'
      ];

      const context = {
        reasonForDivorceDesertionAgreed: 'Yes',
        divorceWho: 'wife'
      };

      testExistenceCYA(done, underTest, content,
        contentToExist, valuesToExist, context);
    });
  });
});
