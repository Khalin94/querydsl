package study.querydsl;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.NonUniqueResultException;
import com.querydsl.core.QueryResults;
import com.querydsl.core.Tuple;
import com.querydsl.core.types.ExpressionUtils;
import com.querydsl.core.types.Predicate;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.CaseBuilder;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.impl.JPAQueryFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import study.querydsl.dto.MemberDto;
import study.querydsl.dto.QUserDto;
import study.querydsl.dto.UserDto;
import study.querydsl.entity.Member;
import study.querydsl.entity.QMember;
import study.querydsl.entity.Team;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.PersistenceContext;
import javax.persistence.PersistenceUnit;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static study.querydsl.entity.QMember.member;
import static study.querydsl.entity.QTeam.*;

@Transactional
@SpringBootTest
public class QuerydslBasicTest {

    @PersistenceContext
    EntityManager em;

    JPAQueryFactory queryFactory;

    private Member createMember(String username, int age) {
        return createMember(username, age, null);
    }

    private Member createMember(String username, int age, Team team) {
        return Member.builder()
                     .username(username)
                     .age(age)
                     .team(team)
                     .build();
    }

    @BeforeEach
    void before() {
        queryFactory = new JPAQueryFactory(em);
        Team teamA = Team.builder()
                         .name("teamA")
                         .build();
        Team teamB = Team.builder()
                         .name("teamB")
                         .build();

        em.persist(teamA);
        em.persist(teamB);

        Member member1 = Member.builder()
                               .username("member1")
                               .age(10)
                               .team(teamA)
                               .build();
        Member member2 = Member.builder()
                               .username("member2")
                               .age(20)
                               .team(teamA)
                               .build();
        Member member3 = Member.builder()
                               .username("member3")
                               .age(30)
                               .team(teamB)
                               .build();

        Member member4 = Member.builder()
                               .username("member4")
                               .age(40)
                               .team(teamB)
                               .build();
        em.persist(member1);
        em.persist(member2);
        em.persist(member3);
        em.persist(member4);
    }

    @DisplayName("jpql을 이용해 member1을 찾을 수 있다.")
    @Test
    void jpqlTest() {
        // find member1
        Member member = em.createQuery("select m from Member m where username = :username", Member.class)
                          .setParameter("username", "member1")
                          .getSingleResult();

        assertThat(member.getUsername()).isEqualTo("member1");
    }

    @DisplayName("querydsl을 이용해 member1을 찾을 수 있다")
    @Test
    public void querydslTest() {
//        JPAQueryFactory queryFactory = new JPAQueryFactory(em);
//        QMember m = new QMember("m");
//        QMember m = QMember.member;

//        Member member = queryFactory.select(QMember.member)
//                                     .from(QMember.member)
//                                     .where(QMember.member.username.eq("member1"))
//                                     .fetchOne();
        Member member1 = queryFactory.select(member)
                                     .from(member)
                                     .where(member.username.eq("member1"))
                                     .fetchOne();

        assertThat(member1.getUsername()).isEqualTo("member1");

    }

    @Test
    public void search() {
        //given
        //when
        Member member1 = queryFactory.selectFrom(member)
                                     .where(member.age.eq(10)
                                                      .and(member.username.eq("member1"))
                                     )
                                     .fetchOne();

        //then
        assertThat(member1.getUsername()).isEqualTo("member1");
    }

    @Test
    public void searchAndParam() {
        //given
        //when
        Member member1 = queryFactory.selectFrom(member)
                                     .where(
                                             member.username.eq("member1"),
                                             member.age.between(10, 30)
                                     )
                                     .fetchOne();

        //then
        assertThat(member1.getUsername()).isEqualTo("member1");
        assertThat(member1.getAge()).isEqualTo(10);
    }

    @Test
    public void fetchTest() {
        //given
        //when
        List<Member> members = queryFactory.selectFrom(member)
                                           .where(member.username.contains("member"))
                                           .fetch();

        //then
        assertThat(members.size()).isEqualTo(4);

        // 리스트가 반환되는 쿼리에 fetchOne을 사용하면 javax.persistence.NonUniqueResultException 이 발생한다.
        assertThatThrownBy(() -> {
            queryFactory.selectFrom(member)
                        .fetchOne();
        }).isInstanceOf(NonUniqueResultException.class);

        //fetchFirst = limit 1과 같다
        Member member1 = queryFactory.selectFrom(member)
                                     .orderBy(member.username.asc())
                                     .fetchFirst();
        assertThat(member1.getUsername()).isEqualTo("member1");

        QueryResults<Member> results = queryFactory.selectFrom(member)
                                                   .fetchResults();

        long total = results.getTotal();
        List<Member> results1 = results.getResults();

        assertThat(total).isEqualTo(4);
        assertThat(results1.get(0)
                           .getUsername()).isEqualTo("member1");

    }

    @Test
    public void orderTest() {
        //given
        Member member5 = createMember(null, 50);
        Member member6 = createMember("member6", 50);
        Member member7 = createMember("member7", 50);
        em.persist(member5);
        em.persist(member6);
        em.persist(member7);
        //when
        List<Member> members = queryFactory.selectFrom(member)
                                           .orderBy(member.age.desc(), member.username.asc()
                                                                                      .nullsLast()) // null 이면 마지막에 나온다.
                                           .fetch();

        //then
        assertThat(members.get(0)
                          .getUsername()).isEqualTo("member6");
        assertThat(members.get(1)
                          .getUsername()).isEqualTo("member7");
        assertThat(members.get(2)
                          .getUsername()).isNull();

    }

    @Test
    public void paging1() {
        //given
        //when
        List<Member> members = queryFactory.selectFrom(member)
                                           .orderBy(member.username.desc())
                                           .offset(1)
                                           .limit(2)
                                           .fetch();

        for (Member member1 : members) {
            System.out.println("member1 = " + member1);
        }

        //then
        assertThat(members.size()).isEqualTo(2);
    }

    @Test
    public void paging2() {
        //given
        //when
        QueryResults<Member> results = queryFactory.selectFrom(member)
                                                   .orderBy(member.username.desc())
                                                   .offset(1)
                                                   .limit(2)
                                                   .fetchResults();

        //then
        assertThat(results.getTotal()).isEqualTo(4);
        assertThat(results.getResults()
                          .size()).isEqualTo(2);
        assertThat(results.getLimit()).isEqualTo(2);
        assertThat(results.getOffset()).isEqualTo(1);
    }

    @Test
    public void aggregation() throws Exception {
        //given
        //when
        List<Tuple> memberAggregation = queryFactory.select(member.count(),
                                                            member.age.sum(),
                                                            member.age.max(),
                                                            member.age.min(),
                                                            member.age.avg()
                                                    )
                                                    .from(member)
                                                    .fetch();

        Tuple tuple = memberAggregation.get(0);

        //then
        assertThat(tuple.get(member.count())).isEqualTo(4);
        assertThat(tuple.get(member.age.sum())).isEqualTo(100);
        assertThat(tuple.get(member.age.max())).isEqualTo(40);
        assertThat(tuple.get(member.age.min())).isEqualTo(10);
        assertThat(tuple.get(member.age.avg())).isEqualTo(25);
    }

    @Test
    public void groupBy() throws Exception {
        //given
        //when
        List<Tuple> fetch = queryFactory.select(team.name, member.age.avg())
                                        .from(member)
                                        .join(member.team, team)
                                        .groupBy(team.name)
                                        .fetch();

        Tuple teamA = fetch.get(0);
        Tuple teamB = fetch.get(1);


        //then
        assertThat(teamA.get(team.name)).isEqualTo("teamA");
        assertThat(teamA.get(member.age.avg())).isEqualTo(15);

        assertThat(teamB.get(team.name)).isEqualTo("teamB");
        assertThat(teamB.get(member.age.avg())).isEqualTo(35);

        List<Tuple> fetch1 = queryFactory.select(team.name, member.age.max())
                                         .from(team)
                                         .join(member)
                                         .on(team.id.eq(member.team.id))
                                         .groupBy(team.name)
                                         .fetch();
        Tuple teamAA = fetch1.get(0);
        Tuple teamBB = fetch1.get(1);

        assertThat(teamAA.get(team.name)).isEqualTo("teamA");
        assertThat(teamAA.get(member.age.max())).isEqualTo(20);

        assertThat(teamBB.get(team.name)).isEqualTo("teamB");
        assertThat(teamBB.get(member.age.max())).isEqualTo(40);

    }

    @Test
    public void join() throws Exception {
        //given
//        Member teamA = Member.builder()
//                             .username("teamA")
//                             .build();
//        Member teamB = Member.builder()
//                             .username("teamB")
//                             .build();
//        em.persist(teamA);
//        em.persist(teamB);
        //when
        List<Member> teamAList = queryFactory.select(member)
                                             .from(member)
                                             .join(member.team, team)
                                             .where(team.name.eq("teamA"))
                                             .fetch();

        //then
        assertThat(teamAList).extracting("username")
                             .containsExactly("member1", "member2");
    }

    @Test
    public void thetaJoin() throws Exception {
        //given
        Member teamA = Member.builder()
                             .username("teamA")
                             .build();
        Member teamB = Member.builder()
                             .username("teamB")
                             .build();
        Member teamC = Member.builder()
                             .username("teamC")
                             .build();
        em.persist(teamA);
        em.persist(teamB);
        em.persist(teamC);
        //when
        // 이렇게 조인하는 건 외부조인 불가능 -> on을 사용해야 된다.
        List<Member> members = queryFactory.select(member)
                                           .from(member, team)
                                           .where(member.username.eq(team.name))
                                           .fetch();

        //then
        assertThat(members).extracting("username")
                           .containsExactly("teamA", "teamB");
    }

    @Test
    public void joinFiltering() throws Exception {
        //given

        //when
        List<Tuple> result = queryFactory.select(member, team)
                                         .from(member)
                                         .leftJoin(member.team, team)
                                         .on(team.name.eq("teamA"))
                                         .fetch();

        for (Tuple tuple : result) {
            System.out.println("tuple = " + tuple);
        }
        //then

    }

    @Test
    public void joinOnNoRelation() throws Exception {
        //given
        Member teamA = Member.builder()
                             .username("teamA")
                             .build();
        Member teamB = Member.builder()
                             .username("teamB")
                             .build();
        Member teamC = Member.builder()
                             .username("teamC")
                             .build();
        em.persist(teamA);
        em.persist(teamB);
        em.persist(teamC);
        //when
        List<Tuple> result = queryFactory.select(member, team)
                                         .from(member)
                                         .leftJoin(team)
                                         .on(member.username.eq(team.name))
                                         .fetch();

        for (Tuple tuple : result) {
            System.out.println("tuple = " + tuple);
        }

        List<Tuple> joinTeamResult = queryFactory.select(member, team)
                                                 .from(member)
                                                 .join(team)
                                                 .on(member.team.eq(team))
                                                 .fetch();
        for (Tuple tuple : joinTeamResult) {
            System.out.println("tuple = " + tuple);
        }
        //then
    }

    @PersistenceUnit
    EntityManagerFactory emf;

    @Test
    public void fetchJoinNotUse() throws Exception {
        em.flush();
        em.clear();
        //given
        //when
        Member member1 = queryFactory.select(member)
                                     .from(member)
                                     .where(member.username.eq("member1"))
                                     .fetchOne();

        boolean isLoaded = emf.getPersistenceUnitUtil()
                              .isLoaded(member1.getTeam());

        //then
        assertThat(isLoaded).as("페치조인 미적용")
                            .isFalse();
    }

    @Test
    public void fetchJoinUse() throws Exception {
        //given
        em.flush();
        em.clear();
        //when
        Member member1 = queryFactory.select(member)
                                     .from(member)
                                     .join(member.team, team)
                                     .fetchJoin()
                                     .where(member.username.eq("member1"))
                                     .fetchOne();

        boolean isLoaded = emf.getPersistenceUnitUtil()
                              .isLoaded(member1.getTeam());

        //then
        assertThat(isLoaded).as("페치조인 적용")
                            .isTrue();
    }

    @Test
    public void whereSubQuery() throws Exception {
        //given
        //when
        QMember subMember = new QMember("subMember");
        List<Member> maxAgeMembers = queryFactory.select(member)
                                                 .from(member)
                                                 .where(member.age.eq(
                                                         JPAExpressions.select(subMember.age.max())
                                                                       .from(subMember)
                                                 ))
                                                 .fetch();
        for (Member maxAgeMember : maxAgeMembers) {
            System.out.println("maxAgeMember = " + maxAgeMember);
        }

        //then
        assertThat(maxAgeMembers).extracting("age")
                                 .containsExactly(40);
    }

    @Test
    public void whereSubQueryGoe() throws Exception {
        //given
        //when
        QMember subMember = new QMember("subMember");
        List<Member> goeAvgAgeMembers = queryFactory.select(member)
                                                    .from(member)
                                                    .where(
                                                            member.age.goe(
                                                                    JPAExpressions.select(subMember.age.avg())
                                                                                  .from(subMember)
                                                            )
                                                    )
                                                    .fetch();

        for (Member goeAvgAgeMember : goeAvgAgeMembers) {
            System.out.println("goeAvgAgeMember = " + goeAvgAgeMember);
        }

        //then
        assertThat(goeAvgAgeMembers).extracting("username")
                                    .containsExactly("member3", "member4");
    }

    @Test
    public void subQueryIn() throws Exception {
        //given
        //when
        QMember subMember = new QMember("subMember");
        List<Member> gtMembers = queryFactory
                .select(member)
                .from(member)
                .where(member.age.in(
                        JPAExpressions.select(subMember.age)
                                      .from(subMember)
                                      .where(subMember.age.gt(10))
                ))
                .fetch();

        for (Member gtMember : gtMembers) {
            System.out.println("gtMember = " + gtMember);
        }

        //then
        assertThat(gtMembers).extracting("age")
                             .containsExactly(20, 30, 40);
    }

    @Test
    public void selectSubQuery() throws Exception {
        //given
        QMember subMember = new QMember("subMember");
        //when
        List<Tuple> fetch = queryFactory.select(member.username, JPAExpressions.select(subMember.age.avg())
                                                                               .from(subMember))
                                        .from(member)
                                        .fetch();


        System.out.println("fetch = " + fetch);

        //then
    }

    @Test
    public void basicCase() throws Exception {
        //given
        //when
        List<String> fetch = queryFactory.select(
                                                 member.age.when(10)
                                                           .then("열살").
                                                           when(20)
                                                           .then("스무살")
                                                           .otherwise("기타")
                                         )
                                         .from(member)
                                         .fetch();

        for (String s : fetch) {
            System.out.println("s = " + s);
        }

        List<Member> fetch1 = queryFactory.selectFrom(member)
                                          .fetch();

        List<String> memberAge = new ArrayList<>();
        for (Member f : fetch1) {
            if (f.getAge() == 10) memberAge.add("열살");
            else if (f.getAge() == 20) memberAge.add("스무살");
            else memberAge.add("기타");
        }

        for (String s : memberAge) {
            System.out.println("s = " + s);
        }

        //then
    }

    @Test
    public void complexCase() throws Exception {
        //given
        //when
        List<String> ageList = queryFactory.select(new CaseBuilder().when(member.age.between(0, 20))
                                                                    .then("0살 ~ 20살")
                                                                    .when(member.age.between(21, 30))
                                                                    .then("21살 ~ 30살")
                                                                    .otherwise("기타")
                                           )
                                           .from(member)
                                           .fetch();

        for (String s : ageList) {
            System.out.println("s = " + s);
        }

        //then
        assertThat(ageList.size()).isEqualTo(4);
    }

    @Test
    public void constant() throws Exception {
        //given
        //when
        List<Tuple> constantMemberList = queryFactory.select(member, Expressions.constant("A"))
                                                     .from(member)
                                                     .fetch();

        for (Tuple tuple : constantMemberList) {
            System.out.println("tuple = " + tuple);
        }

        //then
    }

    @Test
    public void concat() throws Exception {
        //given
        //when
        List<String> result = queryFactory.select(member.username.concat("_")
                                                                 .concat(member.age.stringValue()))
                                          .from(member)
                                          .where(member.username.eq("member1"))
                                          .fetch();

        System.out.println("result = " + result);

        //then
        assertThat(result).contains("member1_10");
    }

    @Test
    public void simpleProjection() throws Exception {
        //given
        //when
        List<String> usernames = queryFactory.select(member.username)
                                             .from(member)
                                             .fetch();

        System.out.println("usernames = " + usernames);

        //then
    }

    @Test
    public void tupleProjection() throws Exception {
        //given
        //when
        List<Tuple> tuples = queryFactory.select(member.username, member.age)
                                         .from(member)
                                         .fetch();

        for (Tuple tuple : tuples) {
            String username = tuple.get(member.username);
            Integer age = tuple.get(member.age);
            System.out.println("username = " + username);
            System.out.println("age = " + age);
            System.out.println();
        }

        //then
    }

    @Test
    public void projectionsJPQL() throws Exception {
        //given
        List<MemberDto> members = em.createQuery("select new study.querydsl.dto.MemberDto(m.username, m.age) from Member m", MemberDto.class)
                                    .getResultList();

        //when
        for (MemberDto memberDto : members) {
            System.out.println("memberDto = " + memberDto);
        }

        //then
    }

    @Test
    public void findDtoBySetter() throws Exception {
        //given
        List<MemberDto> result = queryFactory.select(Projections.bean(MemberDto.class, member.username, member.age))
                                             .from(member)
                                             .fetch();
        //when
        System.out.println("result = " + result);

        //then
    }

    @Test
    public void findDtoByField() throws Exception {
        //given
        List<MemberDto> result = queryFactory.select(Projections.fields(MemberDto.class, member.username, member.age))
                                             .from(member)
                                             .fetch();
        //when

        for (MemberDto memberDto : result) {
            System.out.println("memberDto = " + memberDto);
        }

        //then
    }

    @Test
    public void findDtoByConstructor() throws Exception {
        //given
        List<MemberDto> result = queryFactory.select(Projections.constructor(MemberDto.class, member.username, member.age))
                                             .from(member)
                                             .fetch();
        //when
        for (MemberDto memberDto : result) {
            System.out.println("memberDto = " + memberDto);
        }

        //then
    }

    @Test
    public void findUserDtoNotMatchFieldName() throws Exception {
        //given
//        List<UserDto> result = queryFactory.select(
//                            Projections.bean(UserDto.class,
//                                             Expressions.as(member.username, "name")
//                                             , member.age)
//                                           )
//                                           .from(member)
//                                           .fetch();

        List<UserDto> result = queryFactory.select(
                                                   Projections.bean(UserDto.class,
                                                           member.username.as("name"),
                                                           member.age)
                                           )
                                           .from(member)
                                           .fetch();

        //when
        for (UserDto userDto : result) {
            System.out.println("userDto = " + userDto);
        }
        //then
        assertThat(result).extracting("name")
                          .contains("member1", "member2", "member3", "member4");
    }

    @Test
    public void findUserDtoNotMatchFieldNameUseExpressions() throws Exception {
        //given
        QMember subMember = new QMember("subMember");
//        List<UserDto> result = queryFactory.select(
//                            Projections.bean(UserDto.class,
//                                             member.username.as("name")
//                                             , Expressions.as(JPAExpressions.select(subMember.age.max()).from(subMember), "age")
//                            )
//                                           )
//                                           .from(member)
//                                           .fetch();

        List<UserDto> result = queryFactory.select(
                                                   Projections.bean(UserDto.class,
                                                           member.username.as("name")
                                                           , ExpressionUtils.as(JPAExpressions.select(subMember.age.max())
                                                                                              .from(subMember), "age")
                                                   )
                                           )
                                           .from(member)
                                           .fetch();


        //when
        for (UserDto userDto : result) {
            System.out.println("userDto = " + userDto);
        }
        //then
        assertThat(result).extracting("name")
                          .contains("member1", "member2", "member3", "member4");
    }

    @Test
    public void findUserDtoNotMatchNameConstructor() throws Exception {
        //given
        // constructor로 생성하는 경우 별도로 이름을 맞춰주지 않아도 된다.
        // constructor는 type을 보고 데이터를 넣어준다.
//        List<UserDto> result = queryFactory.select(Projections.constructor(UserDto.class, member.username, member.age))
//                                          .from(member)
//                                          .fetch();
        QMember subMember = new QMember("subMember");
        List<UserDto> result = queryFactory.select(Projections.constructor(UserDto.class, member.username, JPAExpressions.select(subMember.age.max())
                                                                                                                         .from(subMember)))
                                           .from(member)
                                           .fetch();
        //when
        for (UserDto userDto : result) {
            System.out.println("userDto = " + userDto);
        }

        assertThat(result).extracting("age")
                          .contains(40);

        //then
    }

    @Test
    public void findUserByQueryProjectionAnnotation() throws Exception {
        //given
        // @QueryProjection 사용이 가장 깔끔한 방법이지만 Dto가 queryDsl에 종속된다.
        List<UserDto> result = queryFactory.select(new QUserDto(member.username, member.age))
                                           .from(member)
                                           .fetch();
        //when
        for (UserDto userDto : result) {
            System.out.println("userDto = " + userDto);
        }

        //then
    }

    @Test
    public void dynamicQueryUseBooleanBuilder() throws Exception {
        //given
        String username = "member1";
        Integer age = null;
        List<Member> members = searchUser1(username, age);

        //when
        for (Member member1 : members) {
            System.out.println("member1 = " + member1);
        }
        //then
        assertThat(members).extracting("username")
                           .containsExactly("member1");
    }

    private List<Member> searchUser1(String usernameCond, Integer ageCond) {

        BooleanBuilder builder = new BooleanBuilder();

        if (usernameCond != null) {
            builder.and(member.username.eq(usernameCond));
        }

        if (ageCond != null) {
            builder.and(member.age.eq(ageCond));
        }


        return queryFactory.select(member)
                           .from(member)
                           .where(builder)
                           .fetch();
    }

    @Test
    public void dynamicQueryUseWhereParam() throws Exception {
        //given
        String username = "member1";
        Integer age = 10;
        //when
        List<Member> members = searchUser2(username, age);
//        List<Member> members = searchUser3(username, age);

        for (Member member1 : members) {
            System.out.println("member1 = " + member1);
        }

        //then
    }

    private List<Member> searchUser2(String usernameCond, Integer ageCond) {
        return queryFactory.select(member)
                           .from(member)
                           .where(usernameEq(usernameCond), ageEq(ageCond))
                           .fetch();
    }

    private Predicate usernameEq(String usernameCond) {
        return usernameCond != null ? member.username.eq(usernameCond) : null;
    }

    private Predicate ageEq(Integer ageCond) {
        return ageCond != null ? member.age.eq(ageCond) : null;
    }

    private BooleanExpression usernameEq1(String usernameCond) {
        return usernameCond != null ? member.username.eq(usernameCond) : null;
    }

    private BooleanExpression ageEq1(Integer ageCond) {
        return ageCond != null ? member.age.eq(ageCond) : null;
    }

    private List<Member> searchUser3(String username, Integer age) {
        return queryFactory.select(member)
                           .from(member)
                           .where(allEq(username, age))
                           .fetch();
    }

    private BooleanExpression allEq(String usernameCond, Integer ageCond) {
        return usernameEq1(usernameCond).and(ageEq1(ageCond));
    }

    @Test
    public void bulkUpdate() throws Exception {
        //given
        //when
        long result = queryFactory.update(member)
                               .set(member.username, "비회원")
                               .where(member.age.lt(28))
                               .execute();

        System.out.println("result = " + result);

        // 벌크 연산 시 persistence context 에 업데이트가 되지 않는다!
        // 그러므로 벌크로 execute 하는 경우 flush(), clear()를 해줘야 된다.
        em.flush();
        em.clear();

        List<Member> memberList = queryFactory.select(member)
                                         .from(member)
                                         .fetch();

        for (Member member1 : memberList) {
            System.out.println("member1 = " + member1);
        }

        //then
        assertThat(memberList.get(0).getUsername()).isEqualTo("비회원");
        assertThat(memberList.get(1).getUsername()).isEqualTo("비회원");
    }

    @Test
    public void bulkAdd() throws Exception {
        //given
        //when
        long result = queryFactory.update(member)
                                   .set(member.age, member.age.add(11)) // 빼기를 하고 싶으면 add() 안에 값을 -로 주면 된다. add(-10)
                                   .execute();

        em.flush();
        em.clear();

        System.out.println("result = " + result);

        List<Member> memberList = queryFactory.selectFrom(member)
                                         .fetch();

        for (Member member1 : memberList) {
            System.out.println("member1 = " + member1);
        }

        //then
        assertThat(memberList).extracting("age").contains(21, 31, 41, 51);
    }

    @Test
    public void bulkMultiply() throws Exception {
        //given
        //when
        queryFactory.update(member)
                .set(member.age, member.age.multiply(5))
                .execute();

        em.flush();
        em.clear();

        List<Member> memberList = queryFactory.selectFrom(member)
                                         .fetch();

        //then
        assertThat(memberList).extracting("age").contains(50, 100, 150, 200);
    }

    @Test
    public void bulkDelete() throws Exception {
        //given
        //when
        queryFactory.delete(member)
                .where(member.age.loe(20)).execute();

        em.flush();
        em.clear();

        List<Member> memberList = queryFactory.selectFrom(member)
                                         .fetch();

        for (Member member1 : memberList) {
            System.out.println("member1 = " + member1);
        }

        //then
        assertThat(memberList.size()).isEqualTo(2);
    }

    @Test
    public void sqlFunction() throws Exception {
        //given
        //when
        List<String> result = queryFactory.select(Expressions.stringTemplate("function('replace', {0}, {1}, {2})", member.username, "member", "M"))
                                         .from(member)
                                         .fetch();

        System.out.println("result = " + result);

        //then
    }
    
    @Test
    public void sqlFunction2() throws Exception {
        //given
        //when
        List<Member> result = queryFactory.select(member)
                                          .from(member)
//                                         .where(member.username.eq(Expressions.stringTemplate("function('lower', {0})", member.username)))
                                          // ANSI 표준에 있는 함수의 경우 querydsl 에서 지원하는 것들이 있다.
                                          .where(member.username.eq(member.username.lower()))
                                          .fetch();

        for (Member member1 : result) {
            System.out.println("member1 = " + member1);
        }

        //then
    }

}
