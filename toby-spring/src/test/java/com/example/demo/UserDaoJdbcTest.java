package com.example.demo;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

import com.example.demo.user.Level;
import com.example.demo.user.User;
import com.example.demo.user.repository.UserDao;
import com.example.demo.user.repository.UserDaoJdbc;
import java.sql.SQLException;
import java.util.List;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest
public class UserDaoJdbcTest {

    @Autowired
    private ApplicationContext applicationContext;

    private UserDao dao;
    private User user1;
    private User user2;
    private User user3;

    @Before
    public void setUp() {
        dao = applicationContext.getBean("userDao",UserDaoJdbc.class);
        this.user1 = new User("id1", "name1", "springno1", Level.BASIC, 1, 0, "test1@naver.com");
        this.user2 = new User("id2", "name2", "springno2", Level.SILVER, 55, 10, "test2@naver.com");
        this.user3 = new User("id3", "name3", "springno3", Level.GOLD, 100, 40, "test3@naver.com");
    }

    @Test
    public void getAll() {
        // given
        dao.deleteAll();
        List<User> userList0 = dao.getAll();

        assertThat(userList0.size(), is(0));

        dao.add(user1);
        List<User> userList1 = dao.getAll();
        assertThat(userList1.size(), is(1));
        checkSameUser(user1, userList1.get(0));

        dao.add(user2);
        List<User> userList2 = dao.getAll();
        assertThat(userList2.size(), is(2));
        checkSameUser(user1, userList2.get(0));
        checkSameUser(user2, userList2.get(1));

        dao.add(user3);
        List<User> userList3 = dao.getAll();
        assertThat(userList3.size(), is(3));
        checkSameUser(user1, userList3.get(0));
        checkSameUser(user2, userList3.get(1));
        checkSameUser(user3, userList3.get(2));
    }

    @Test
    public void update() {
        dao.deleteAll();

        dao.add(user1);
        dao.add(user2);

        user1.setName("오민규");
        user1.setPassword("springno6");
        user1.setLevel(Level.GOLD);
        user1.setLogin(1000);
        user1.setRecommend(999);
        user1.setEmail("test1@naver.com");
        dao.update(user1);

        checkSameUser(user1, dao.get(user1.getId()));
        checkSameUser(user2, dao.get(user2.getId()));
    }

    private void checkSameUser(User givenUser, User actualUser) {
        assertThat(givenUser.getId(), is(actualUser.getId()));
        assertThat(givenUser.getName(), is(actualUser.getName()));
        assertThat(givenUser.getPassword(), is(actualUser.getPassword()));
        assertThat(givenUser.getLevel(), is(actualUser.getLevel()));
        assertThat(givenUser.getLogin(), is(actualUser.getLogin()));
        assertThat(givenUser.getRecommend(), is(actualUser.getRecommend()));
    }

    @Test
    public void addAndGet() {
        dao.deleteAll();
        assertThat(dao.getCount(), is(0));

        dao.add(user1);
        dao.add(user2);
        assertThat(dao.getCount(), is(2));

        User getUser1 = dao.get(user1.getId());
        assertThat(getUser1.getName(), is(user1.getName()));
        assertThat(getUser1.getPassword(), is(user1.getPassword()));

        User getUser2 = dao.get(user2.getId());
        assertThat(getUser2.getName(), is(user2.getName()));
        assertThat(getUser2.getPassword(), is(user2.getPassword()));
    }

    @Test(expected = EmptyResultDataAccessException.class)
    public void getUserFailure() {
        dao.deleteAll();
        assertThat(dao.getCount(), is(0));

        dao.get("unkonw_id");
    }

    @Test
    public void count() {
        dao.deleteAll();
        assertThat(dao.getCount(), is(0));

        dao.add(user1);
        assertThat(dao.getCount(), is(1));

        dao.add(user2);
        assertThat(dao.getCount(), is(2));

        dao.add(user3);
        assertThat(dao.getCount(), is(3));
    }

}
