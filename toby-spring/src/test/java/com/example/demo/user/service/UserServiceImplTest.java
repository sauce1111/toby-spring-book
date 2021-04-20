package com.example.demo.user.service;

import static com.example.demo.user.service.UserServiceImpl.MIN_LOGCOUNT_FOR_SILVER;
import static com.example.demo.user.service.UserServiceImpl.MIN_RECOMMEND_FOR_GOLD;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.core.IsNull.notNullValue;
import static org.junit.Assert.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import com.example.demo.user.Level;
import com.example.demo.user.User;
import com.example.demo.user.repository.UserDao;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mail.MailException;
import org.springframework.mail.MailSender;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;

@RunWith(SpringRunner.class)
@SpringBootTest
public class UserServiceImplTest {

    @Autowired
    private UserService userService;

    @Autowired
    private UserDao dao;

    @Autowired
    private UserService testUserService;

    @Autowired
    private PlatformTransactionManager transactionManager;

    private List<User> users;

    @Before
    public void setUp() throws Exception {
        users = Arrays.asList(
                new User("id1", "name1", "p1", Level.BASIC, MIN_LOGCOUNT_FOR_SILVER-1, 0, "test1@naver.com"),
                new User("id2", "name2", "p1", Level.BASIC, MIN_LOGCOUNT_FOR_SILVER, 0, "test2@naver.com"),
                new User("id3", "name3", "p1", Level.SILVER, 60, MIN_RECOMMEND_FOR_GOLD-1, "test3@naver.com"),
                new User("id4", "name4", "p1", Level.SILVER, 60, MIN_RECOMMEND_FOR_GOLD, "test4@naver.com"),
                new User("id5", "name5", "p1", Level.GOLD, 100, Integer.MAX_VALUE, "test5@naver.com")
        );
    }

    @Test
    public void bean() {
        assertThat(this.userService, is(notNullValue()));
    }

//    @Test
//    public void advisorAutoProxyCreator() {
//        assertThat(testUserService instanceof Proxy, is(true));
//    }

    @Test
    public void upgradeLevels() {
        //given
        UserDao mockUserDao = mock(UserDao.class);
        MailSender mockMailSender = mock(MailSender.class);
        final UserServiceImpl userServiceImpl = new UserServiceImpl(mockUserDao, mockMailSender);
        given(mockUserDao.getAll()).willReturn(this.users);

        //when
        userServiceImpl.upgradeLevels();

        //then
        verify(mockUserDao, times(2)).update(any(User.class));
        verify(mockUserDao).update(users.get(1));
        verify(mockUserDao, times(2)).update(any(User.class));
        assertThat(users.get(1).getLevel(), is(Level.SILVER));
        verify(mockUserDao).update(users.get(3));
        assertThat(users.get(3).getLevel(), is(Level.GOLD));

        ArgumentCaptor<SimpleMailMessage> mailMessageArg = ArgumentCaptor.forClass(SimpleMailMessage.class);

        verify(mockMailSender, times(2)).send(mailMessageArg.capture());
        List<SimpleMailMessage> mailMessages = mailMessageArg.getAllValues();
        assertThat(mailMessages.get(0).getTo()[0], is(users.get(1).getEmail()));
        assertThat(mailMessages.get(1).getTo()[0], is(users.get(3).getEmail()));
    }

    @Test
    public void testAdd() {
        dao.deleteAll();

        User userWithLevel = users.get(4);
        User userWithoutLevel = users.get(0);
        userWithoutLevel.setLevel(null);

        userService.add(userWithLevel);
        userService.add(userWithoutLevel);

        User userWithLevelRead = dao.get(userWithLevel.getId());
        User userWithoutLevelRead = dao.get(userWithoutLevel.getId());

        assertThat(userWithLevelRead.getLevel(), is(userWithLevel.getLevel()));
        assertThat(userWithoutLevelRead.getLevel(), is(Level.BASIC));
    }

//    @Test
//    @DirtiesContext
//    public void upgradeAllOrNothing() {
//
//        dao.deleteAll();
//        for (User user : users) dao.add(user);
//
//        try {
//            testUserService.upgradeLevels();
//            fail("TestUserServiceException expected");
//        } catch (TestUserService.TestUserServiceException e) {
//        }
//
//        checkLevel(users.get(1),false);
//    }

    @Test
    public void readOnlyTransactionAttribute() {
        testUserService.getAll();
    }

    private void checkLevel(User user, boolean upgraded) {
        User userUpdate = dao.get(user.getId());

        if (upgraded) {
            assertThat(userUpdate.getLevel(), is(user.getLevel().nextLevel()));
        } else {
            assertThat(userUpdate.getLevel(), is(user.getLevel()));
        }
    }

    @Test
    public void transactionSync() {
        DefaultTransactionDefinition txDefinition = new DefaultTransactionDefinition();
        TransactionStatus txStatus = transactionManager.getTransaction(txDefinition);

        try {
            userService.deleteAll();
            userService.add(users.get(0));
            userService.add(users.get(1));
        } finally {
            transactionManager.rollback(txStatus);
        }
    }

    static class MockMailSender implements MailSender {

        private List<String> requests = new ArrayList<>();

        public List<String> getRequests() {
            return requests;
        }

        @Override
        public void send(SimpleMailMessage mailMessage) throws MailException {
            requests.add(mailMessage.getTo()[0]); // 전송 요청을 받은 이메일 주소를 저장해둔다. 첫번쨰 수신자 메일이다
        }

        @Override
        public void send(SimpleMailMessage... mailMessage) throws MailException {

        }
    }

    static class MockUserDao implements UserDao {
        private List<User> users;
        private List<User> updated = new ArrayList<>();

        public MockUserDao(List<User> users) {
            this.users = users;
        }

        public List<User> getUpdated(){
            return this.updated;
        }

        @Override
        public List<User> getAll() {
            return this.users;
        }

        @Override
        public void update(User user) {
            updated.add(user);
        }

        @Override
        public void add(User user) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void deleteAll() {
            throw new UnsupportedOperationException();
        }

        @Override
        public User get(String id) {
            throw new UnsupportedOperationException();
        }

        @Override
        public int getCount() {
            throw new UnsupportedOperationException();
        }
    }


}
