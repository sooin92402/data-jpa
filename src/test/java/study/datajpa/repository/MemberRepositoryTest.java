package study.datajpa.repository;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.Sort;
import org.springframework.test.annotation.Rollback;
import org.springframework.transaction.annotation.Transactional;
import study.datajpa.dto.MemberDto;
import study.datajpa.entity.Member;
import study.datajpa.entity.Team;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.awt.print.Pageable;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
@Rollback(value = false)
class MemberRepositoryTest {

    @Autowired MemberRepository memberRepository;
    @Autowired TeamRepository teamRepository;
    @PersistenceContext
    EntityManager em;

    @Autowired MemberQueryRepository memberQueryRepository;

    @Test
    public void testMember () {
        Member member = new Member("memberA");
        Member savedMember = memberRepository.save(member);

        Member findMember = memberRepository.findById(savedMember.getId()).get();

        assertThat(findMember.getId()).isEqualTo(member.getId());
        assertThat(findMember.getUsername()).isEqualTo(member.getUsername());
        assertThat(findMember).isEqualTo(member);
    }

    @Test
    public void basicCRUD(){
        Member member1 = new Member("member1");
        Member member2 = new Member("member2");

        memberRepository.save(member1);
        memberRepository.save(member2);

        //단건 조회 검증
        Member findMember1 = memberRepository.findById(member1.getId()).get();
        Member findMember2 = memberRepository.findById(member2.getId()).get();
        assertThat(findMember1).isEqualTo(member1);
        assertThat(findMember2).isEqualTo(member2);

//      findMember1.setUsername("member!!!!!!!"); //변경감지

        //리스트 조회 검증
        List<Member> all = memberRepository.findAll();
        assertThat(all.size()).isEqualTo(2);

        //카운트 검증
        long count = memberRepository.count();
        assertThat(count).isEqualTo(2);

        //삭제 검증
        memberRepository.delete(member1);
        memberRepository.delete(member2);
        long deletedCount = memberRepository.count();
        assertThat(deletedCount).isEqualTo(0);
    }

    @Test
    public void findByUsernameAndAgeGreaterThan(){
        Member m1 = new Member("AAA", 10);
        Member m2 = new Member("AAA", 20);

        memberRepository.save(m1);
        memberRepository.save(m2);

        List<Member> result = memberRepository.findByUsernameAndAgeGreaterThan("AAA", 15);

        assertThat(result.get(0).getUsername()).isEqualTo("AAA");
        assertThat(result.get(0).getAge()).isEqualTo(20);
        assertThat(result.size()).isEqualTo(1);
    }

    @Test
    public void findHelloBy(){
        List<Member> helloBy = memberRepository.findTop3HelloBy();
    }

    @Test
    public void testNamedQuery(){
        Member m1 = new Member("AAA", 10);
        Member m2 = new Member("BBB", 20);
        memberRepository.save(m1);
        memberRepository.save(m2);

        List<Member> result = memberRepository.findByUsername("AAA");
        Member findMember = result.get(0);
        assertThat(findMember).isEqualTo(m1);

    }

    @Test
    public void testQuery(){
        Member m1 = new Member("AAA", 10);
        Member m2 = new Member("BBB", 20);
        memberRepository.save(m1);
        memberRepository.save(m2);

        List<Member> result = memberRepository.findUser("AAA",10);
        Member findMember = result.get(0);
        assertThat(result.get(0)).isEqualTo(m1);

    }

    @Test
    public void findUsernameList(){
        Member m1 = new Member("AAA", 10);
        Member m2 = new Member("BBB", 20);
        memberRepository.save(m1);
        memberRepository.save(m2);

        List<String> usernameList = memberRepository.findUsernameList();
        for (String s : usernameList) {
            System.out.println("s = " + s);
        }

    }

    @Test
    public void findMemberDto(){
        Team team = new Team("teamA");
        teamRepository.save(team);

        Member m1 = new Member("AAA", 10);
        m1.setTeam(team);
        memberRepository.save(m1);


        List<MemberDto> memberDto = memberRepository.findMemberDto();
        for (MemberDto dto : memberDto) {
            System.out.println("dto = " + dto);
        }
    }

    @Test
    public void findByNames(){
        Member m1 = new Member("AAA", 10);
        Member m2 = new Member("BBB", 20);
        memberRepository.save(m1);
        memberRepository.save(m2);

        List<Member> result = memberRepository.findByNames(Arrays.asList("AAA", "BBB"));
        for (Member member : result) {
            System.out.println("member = " + member);
        }
    }

    @Test
    public void returnType(){
        Member m1 = new Member("AAA", 10);
        Member m2 = new Member("BBB", 20);
        memberRepository.save(m1);
        memberRepository.save(m2);

        /*List<Member> findMember = memberRepository.findListByUsername("AAA");
        System.out.println("findMember = " + findMember);*/

        /*Optional<Member> findMember = memberRepository.findOptionalByUsername("AAA");
        System.out.println("findMember = " + findMember);*/

        /*List<Member> result = memberRepository.findListByUsername("asdfasdf");
        System.out.println("findMember = " + result.size());//size = 0, null은 아님*/

        /*Member findMember = memberRepository.findMemberByUsername("asdfasfd");
        System.out.println("findMember = " + findMember);//null*/

        /*Optional<Member> findMember = memberRepository.findOptionalByUsername("asdfasfd");
        System.out.println("findMember = " + findMember);//findMember = Optional.empty
        //단건조회(Optional뿐 아니라)에 데이터가 1개 이상일 경우 Exception 발생(IncorrectResultSizeDataAccessException)*/

    }

     @Test
     public void paging() {
        //given
        memberRepository.save(new Member("member1",10));
        memberRepository.save(new Member("member2",10));
        memberRepository.save(new Member("member3",10));
        memberRepository.save(new Member("member4",10));
        memberRepository.save(new Member("member5",10));

        int age = 10;
        PageRequest pageRequest = PageRequest.of(0,3, Sort.by(Sort.Direction.DESC, "username"));

        //when
         Page<Member> page = memberRepository.findByAge(age, pageRequest);

         Page<MemberDto> toMap = page.map(member -> new MemberDto(member.getId(), member.getUsername(), null));//엔티티가 아닌 dto로 반환
         
         //then
         List<Member> content = page.getContent();
         long totalElements = page.getTotalElements();

         for (Member member : content) {
             System.out.println("member = " + member);
         }
         System.out.println("totalElements = " + totalElements);

         assertThat(content.size()).isEqualTo(3);
         assertThat(page.getTotalElements()).isEqualTo(5);
         assertThat(page.getNumber()).isEqualTo(0);//페이지 번호 (0부터 시작)
         assertThat(page.getTotalPages()).isEqualTo(2);
         assertThat(page.isFirst()).isTrue();
         assertThat(page.hasNext()).isTrue();

     }

//    @Test
//    public void slice() {
//        //given
//        memberRepository.save(new Member("member1",10));
//        memberRepository.save(new Member("member2",10));
//        memberRepository.save(new Member("member3",10));
//        memberRepository.save(new Member("member4",10));
//        memberRepository.save(new Member("member5",10));
//
//        int age = 10;
//        PageRequest pageRequest = PageRequest.of(0,3, Sort.by(Sort.Direction.DESC, "username"));
//
//        //when
//        Slice<Member> page = memberRepository.findByAge(age, pageRequest);
//
//        //then
//        List<Member> content = page.getContent();
//
//        assertThat(content.size()).isEqualTo(3);
////        assertThat(page.getTotalElements()).isEqualTo(5);
//        assertThat(page.getNumber()).isEqualTo(0);//페이지 번호 (0부터 시작)
////        assertThat(page.getTotalPages()).isEqualTo(2);
//        assertThat(page.isFirst()).isTrue();
//        assertThat(page.hasNext()).isTrue();
//
//    }
    @Test
    public void bulkUpdate(){
        //given
        memberRepository.save(new Member("member1",10));
        memberRepository.save(new Member("member2",19));
        memberRepository.save(new Member("member3",20));
        memberRepository.save(new Member("member4",21));
        memberRepository.save(new Member("member5",40));

        //when
        int resultCount = memberRepository.bulkAgePlus(20);
        em.flush();
        em.clear();//flush,clear없을 경우, member5가 DB는 41로 변경 영속성 컨텍스트 상에서는 40
                    // (벌크연산일 경우->연산 이후 em.flush,em.clear 필수)

        List<Member> result = memberRepository.findByUsername("member5");
        Member member5 = result.get(0);

        System.out.println("member5 = " + member5);

        //then
        assertThat(resultCount).isEqualTo(3);
    }

    @Test
    public void findMemberLazy(){
        //given

            //member1 -> TeamA
            //member2 -> TeamB

        Team teamA = new Team("teamA");
        Team teamB = new Team("teamB");
        teamRepository.save(teamA);
        teamRepository.save(teamB);
        Member member1 = new Member("member1",10, teamA);
        Member member2 = new Member("member2",10, teamB);
        memberRepository.save(member1);
        memberRepository.save(member2);

        em.flush();
        em.clear();

        //when N + 1
        //select Member 1
//        List<Member> members = memberRepository.findAll();//(EntityGraph 사용= fetchJoin)
//        List<Member> members = memberRepository.findMemberFetchJoin();
        List<Member> members = memberRepository.findEntityGraphByUsername("member1");
        for (Member member : members) {
            System.out.println("member = " + member.getUsername());
            System.out.println("member.TeamClass = " + member.getTeam().getClass());
            System.out.println("member = " + member.getTeam().getName());

        }
        //then
    }

    @Test
    public void queryHint(){
        //given
        Member member1 = new Member("member1", 10);
        memberRepository.save(member1);
        em.flush();
        em.clear();

        //when
//        Member findMember = memberRepository.findById(member1.getId()).get();
        Member findMember = memberRepository.findReadonlyByUsername("member1");
        findMember.setUsername("member2");//findReadonlyByUsername -> 변환되지 않음 (ReadOnly)

        em.flush();
    }

    @Test
    public void lock(){
        //given
        Member member1 = new Member("member1", 10);
        memberRepository.save(member1);
        em.flush();
        em.clear();

        //when
        List<Member> result = memberRepository.findLockByUsername("member1");

    }

    @Test
    public void callCustom(){
        List<Member> result = memberRepository.findMemberCustom();

    }


}