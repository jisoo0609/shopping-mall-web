# 쇼핑몰 사이트 구축

일반 사용자는 중고거래가 가능하며, 사업자는 인터넷 쇼핑몰을 운영할 수 있게 해주는
쇼핑몰 사이트를 만들어보자.

요구사항의 내용은 프론트엔드 없이, 백엔드만 개발한다. 별도의 프론트엔드 클라이언트가 존재한다고 생각하고 서버를 만들고, Postman으로 테스트를 한다. 단, CORS는 지금은 고려하지 않는다.

## 기능 설명

---

### 1. 기본 과제

- 사용자 인증 및 권한 처리
- 중고거래 중개하기
- 쇼핑몰 운영하기

---

## 진행 상황

## 필수 요구사항

### 사용자 인증 및 권한 처리

- [x]  요청을 보낸 사용자가 누구인지 구분할 수 있는 인증 체계가 갖춰져야 한다.
- [x]  사용자는 회원가입이 가능하다.
- [x]  사용자의 권한이 관리되어야 한다.

### 중고거래 중개하기

- [x]  물품 등록
- [x]  구매 제안

### 쇼핑몰 운영하기

- [x]  쇼핑몰 개설
- [x]  쇼핑몰 관리
    - 등록과 동시에 물건의 이미지 추가 제외 전부 구현 완료
    - 물품 등록 후에 따로 이미지를 추가하는 방법은 구현되어 있음
- [x]  쇼핑몰 조회
- [x]  쇼핑몰 상품 검색
- [x]  쇼핑몰 상품 구매

## 추가 요구사항

- [ ]  Toss Payments
    - 토스페이먼츠 API 불러오기 성공
    - 결제 진행 과정은 구현되어 있으나 테스트 미완료 → 아직 수행 불가능
---
## 진행 중 어려웠던 상황

- 인증부분
- Jwt토큰 예외처리가 제대로 되어있지 않음
- update → 토큰은 제대로 발급되는데 403포비든 → 권한 설정의 문제
- getAuthorites() → 마찬가지로 null인경우 예외처리가 되어있지 않아 권한을 불러올 수 없는 경우가 생겨 시도 여러번 함
- 권한이 너무 꼬여서 String으로 관리가 어려움
- `AuthenticationFacad` 재설정으로 권한 확인함
- 관리자 계정에서 승인하는 과정
    - 승인 요청이 오면 확인하고 승인 / 거절을 진행해야 하는데 여기까지 구현하지 못함
    - 요청이 오면 요청 리스트 전체 승인 or 전체 거절만 가능
- User 생성할때마다 item을 추가하는 것 데이터 관리에 어려움

  → User와 Item을 ManyToMany로 묶어 중개 테이블을 생성해서 관리함

- 거래 제안 요청 조회에서 아이템을 등록한 사용자와 그저 요청을 보낸 사용자와의 혼선이 존재

  → 두 요청의 엔드포인트를 분리해서 처리함

    - 아이템을 등록한 유저는 {id}를 이용해 item에 종속된 요청리스트를 불러왔고
    - 그저 거래 요청만 확인하는 엔드포인트는 @PathVariable 없이 처리해 접근한 유저가 자신이 보낸 요청을 전부 확인할 수 있도록 처리함
- ItemStatus와 proposalStatus가 연결되지 않음

  → 하나가 바뀌면 다른 하나도 바뀌게 처리하고 싶은데 처리되지 않아 수동으로 두개 실행

---
# 프로젝트 실행 방법

해당 레포지토리를 로컬 저장소로 clone해 InteliJ에서 빌드 후 실행한다.

별도의 설정이나 설치는 필요하지 않으며, 프로젝트를 실행한 후 postman에서 요청을 보내 결과를 확인할 수 있다.

postman 분기 설정을 위한 `Misson.postman_collection.json` 파일이 준비되어 있으며, 해당 파일을 postman에서 열어 각 엔드포인트에 대한 테스트가 가능하다.

현재 테스트 단계이기 때문에 토큰의 기간 설정은 7일로 되어있다. 테스트를 위해 url마다 적절한 User 토큰을 사용해야 한다.

`jpa.hiberante.ddl-auto`의 설정은 `update`로 설정되어 있으며, `data.sql` 파일에서 테스트용 데이터를 일부 추가할 수 있다.

---
# TEST

## Entity

사용자 인증 및 권한 처리를 담당하는 `auth` , 중고거래를 중개하는 `used` , 쇼핑몰을 운영하는 `shop`으로 나누어서 관리된다.

---

# Auth

사용자 인증 및 권한 처리를 담당한다.

## User

### 회원가입

`/users/register`에서 회원가입이 가능하다.

Postman에서  `RequestBody`로 `username`과 `password` 를 전달해야 한다.

```json
{
  "username": "<값을 입력하세요.>",
  "password": "<값을 입력하세요.>"
}
```

해당 정보를 바탕으로 `user_table`에 저장된다.

이때 password는 encode되어 저장된다.

해당 `url`의 접근 권한은 `permitAll()`로 설정되어 있으며, jwt 토큰 없이 누구나 회원가입이 가능하다.

회원가입된 유저의 권한은 `ROLE_INACTIVE_USER`로 설정되어 저장된다.

### 로그인

`/users/login`에서 로그인을 확인할 수 있다.

회원가입된 정보, 즉 DB에 저장된 `user_table`의 `username`(id)과 `password`를 통해 로그인이 가능하다.

마찬가지로 Postman에서  `RequestBody`로 `username`과 `password` 를 전달해야 한다.

```json
{
  "username": "<값을 입력하세요.>",
  "password": "<값을 입력하세요.>"
}
```

해당 `url`의 접근 권한은 `permitAll()`로 설정되어 있으며, jwt 토큰 없이 누구나 로그인 페이지에 접근이 가능하다.

로그인이 성공적으로 완료되면, jwt 토큰을 발급 받을 수 있다.

현재 토큰이 생성되면 토큰의 만료 기한은 7일로 설정되어 있다.

### 필수 정보 업데이트

`/users/{id}/update`에서 사용자가 서비스를 이용하기 위한 필수 정보 입력이 가능하다.

`{id}`로는 `userId`가 주어져야 하며, `RequestBody`로 필수 정보를 업데이트하기 위한 데이터를 전달해야 한다.

```json
{
  "name": "brad",
  "nickname": "nickname7",
  "age": "20",
  "email": "user7@gmail.com",
  "phone": "0101234678"
}
```

이때 해당 `url`에는 권한이 있는 사람만 접근이 가능하기 때문에, Postman의 Authorization에 로그인으로 얻은 barer토큰을 적절히 넣어주어야 한다. 정보를 업데이트 하려는 유저가, 업데이트할 정보의 유저와 같아야 한다.

해당 `url`에서 닉네임, 이름, 연령대, 이메일, 전화번호 정보 입력이 완료되면, 유저의 권한은 `ROLE_USER`로 자동 업데이트 된다.

### 프로필 이미지 업데이트

`/users/{id}/update-image`에서 사용자의 프로필 이미지 업데이트가 가능하다.

`{id}`로는 `userId`가 주어져야 하며, `RequestBody`로 `form-data` `Key=image`, `<File>`이 적절하게 전달되어야 한다.\
![image]()\
이때 해당 `url`에는 권한이 있는 사람만 접근이 가능하기 때문에, Postman의 Authorization에 로그인으로 얻은 barer토큰을 적절히 넣어주어야 한다. 프로필 이미지를 업데이트 하려는 유저가, 프로필 이미지를 업데이트할 정보의 유저와 같아야 한다.

해당 `url`에서 이미지가 업데이트되면 `/media/User/%d`에 이미지가 저장된다.

### 사업자 전환신청

`/users/{id}/request`에서 사업자로 전환 신청이 가능하다.

## Admin

관리자는 서비스와 상관없이 고정된 사용자기 때문에, 서버를 실행할 때 생성되어 DB에 저장되게 설정했다.

### 사용자 전환 신청 목록 확인

`/admin/list`에서 관리자가 사업자로 사용자 전환을 신청한 사용자의 목록을 확인할 수 있다.

해당 `url`은 관리자만 접근이 가능하기 때문에, Postman의 Authorization에 관리자의 정보로 발급받은 토큰을 적절히 입력해야 한다.

### 사용자 전환 신청 승인

`/admin/check`에서 사업자로 사용자 전환 신청 승인이 가능하다.

`RequestParam`으로 `?accept=true` 또는 `?accpet=false`를 전달해 사용자 전환을 승인하거나, 거절이 가능하다.

해당 `url`은 관리자만 접근이 가능하기 때문에, Postman의 Authorization에 관리자의 정보로 발급받은 토큰을 적절히 입력해야 한다.

전환 승인이 승락되면 `USER`는 `ROLE_BUISNIESS_USER`로 승격이 가능하다.

---

# Used

중고거래 중개를 담당한다.

해당 시스템은 비활성 사용자를 제외한 사용자는 누구든지 사용이 가능하다.

## Item

### 중고거래 물품 등록하기

`/used/create`에서 중고거래 물품을 등록할 수 있다.

`RequestBody`로 중고거래 물품을 등록하기 위한 물품 정보의 적절한 전달이 필요하다.

```json
{
  "title": "<값을 입력하세요.>",
  "description": "<값을 입력하세요.>",
  "price": "<값을 입력하세요.>"
}
```

해당 `url`은 비활성 사용자를 제외한 유저만 접근이 가능하기 때문에, Postman의 Authorization에 비활성 사용자 이상 등급의 사용자 토큰을 적절하게 입력해야 한다.

물품이 등록되면 등록된 물품의 상태는 `SALE("판매중")`이 된다.

### 물품 수정하기

`/used/{id}/update`에서 등록한 물품의 정보 수정이 가능하다.

`{id}`로는 `ItemId`가 주어져야 하며, `RequestBody`로 등록한 물품을 수정하기 위한 물품 정보의 적절한 전달이 필요하다.

```json
{
  "title": "<값을 입력하세요.>",
  "description": "<값을 입력하세요.>",
  "price": "<값을 입력하세요.>"
}
```

해당 `url`은 비활성 사용자를 제외한 유저만 접근이 가능하기 때문에, Postman의 Authorization에 비활성 사용자 이상 등급의 사용자 토큰을 적절하게 입력해야 한다. 물품을 등록한 사용자와 물품을 수정하려는 사용자가 같아야 한다.

### 물품 삭제하기

`/used/{id}/delete`에서 등록한 물품의 삭제가 가능하다.

`{id}`로는 `ItemId`가 주어져야 한다.

해당 `url`은 비활성 사용자를 제외한 유저만 접근이 가능하기 때문에, Postman의 Authorization에 비활성 사용자 이상 등급의 사용자 토큰을 적절하게 입력해야 한다. 물품을 등록한 사용자와 물품을 삭제하려는 사용자가 같아야 한다.

### 물품 상세보기

`/used/{id}`에서 등록한 물품의 상세 열람이 가능하다.

`{id}`로는 `ItemId`가 주어져야 한다.

해당 `url`은 비활성 사용자를 제외한 유저만 접근이 가능하기 때문에, Postman의 Authorization에 비활성 사용자 이상 등급의 사용자 토큰을 적절하게 입력해야 한다.

### 물품 이미지 추가하기

`/used/{id}/update-image`에서 등록한 물품의 이미지를 추가할 수 있다.

`{id}`로는 `ItemId`가 주어져야 하며, `RequestBody`로 `form-data` `Key=image`, `<File>`이 적절하게 전달되어야 한다.

해당 `url`은 비활성 사용자를 제외한 유저만 접근이 가능하기 때문에, Postman의 Authorization에 비활성 사용자 이상 등급의 사용자 토큰을 적절하게 입력해야 한다. 물품을 등록한 사용자와 물품에 이미지를 추가하려는 사용자가 같아야 한다.

## Proposal

### 구매 제안하기

`/used/{id}/proposal`에서 등록된 물건에 대한 구매 제안이 가능하다.

`{id}`로는 `ItemId`가 주어져야 한다.

해당 `url`은 비활성 사용자를 제외한 유저만 접근이 가능하기 때문에, Postman의 Authorization에 비활성 사용자 이상 등급의 사용자 토큰을 적절하게 입력해야 한다.

물품을 등록한 사용자와, 제안을 등록한 사용자는 이 제안을 조회할 수 있다.

### 구매 제안 조회하기

- 물품을 등록한 사용자의 경우

  `/proposal/{id}`에서 등록한 물품에 대한 제안들을 확인할 수 있다.

  `{id}`로는 `ItemId`가 주어져야 한다.

  해당 `url`은 비활성 사용자를 제외한 유저만 접근이 가능하기 때문에, Postman의 Authorization에 비활성 사용자 이상 등급의 사용자 토큰을 적절하게 입력해야 한다. 물품을 등록한 사용자와 물품에 대한 제안을 확인하려는 사용자가 같아야 한다.

  `ItemId`를 기준으로 해당하는 구매 제안의 리스트를 조회할 수 있다.

- 제안을 등록한 사용자의 경우

  `/proposal`에서 등록한 제안들을 확인할 수 있다.

  중고거래 물품의 번호와 상관없이, 자신이 등록한 모든 제안을 불러올 수 있다.

  해당 `url`은 비활성 사용자를 제외한 유저만 접근이 가능하기 때문에, Postman의 Authorization에 비활성 사용자 이상 등급의 사용자 토큰을 적절하게 입력해야 한다.


### 구매 제안 수락 / 거절

물품을 등록한 사용자가 구매 제안을 수락 또는 거절할 수 있다.

`/proposal/{id}/accept`에서 구매 제안의 수락 또는 거절이 가능하다.

`{id}`로는 `proposalId`가 주어저야 한다.

`RequestParam`으로 `?accept=true` 또는 `?accpet=false`를 전달해 구매 제안을 수락하거나 거절이 가능하다.

해당 `url`은 비활성 사용자를 제외한 유저만 접근이 가능하기 때문에, Postman의 Authorization에 비활성 사용자 이상 등급의 사용자 토큰을 적절하게 입력해야 한다. 물품을 등록한 사용자와 구매 제안을 수락하려는 사용자가 같아야 한다.

- `?accept=true`: 구매 제안이 수락되는 경우

  구매 제안의 상태는 수락이 된다.

- `?accpet=false`: 구매 제안이 거절되는 경우

  구매 제안의 상태는 거절이 된다.


### 구매 확정

구매 제안을 한 사용자가 구매를 확정할 수 있다.

`/proposal/1/confirm`에서 구매 제안의 확정이 가능하다.

`{id}`로는 `proposalId`가 주어저야 한다.

`RequestParam`으로 `?confirm=true` 또는 `?confirm=false`를 전달해 구매 제안을 수락하거나 거절이 가능하다.

해당 `url`은 비활성 사용자를 제외한 유저만 접근이 가능하기 때문에, Postman의 Authorization에 비활성 사용자 이상 등급의 사용자 토큰을 적절하게 입력해야 한다. 구매 제안을 등록한 사용자와 구매 제안을 확정하려는 사용자가 같아야 한다.

- `?confirm=true`

  구매 확정으로 제안 상태가 변경된다.

  구매 제안이 확정되었기 때문에 물품의 상태는 판매 완료로 변경된다.

  이후, `itemId`를 불러와, 확정되지 않은 다른 구매 제안의 상태는 모두 거절된다.

- `?confirm=false`

  구매 제안이 거절된다.

---
# Shop

쇼핑몰 운영을 담당한다.

## Shop

### 쇼핑몰 개설하기

`/shop/create`에서 사업자 사용자는 쇼핑몰을 개설할 수 있다.

`RequestBody`로 쇼핑몰의 이름, 설명, 쇼핑몰 분류를 적절하게 전달해야 한다.

```
{
	"name": "<값을 입력하세요.>",
	"description": "This is an example shop.",
	"shopCategories": ["<값을 입력하세요.>"]
}
```

해당 `url`은 사업자 사용자 이상의 접근이 가능하기 때문에, Postman의 Authorization에 사업자 사용자 이상 등급의 사용자 토큰을 적절하게 입력해야 한다.

개설된 쇼핑몰의 상태는 `준비중`으로 설정된다.

### 쇼핑몰 수정하기

`/shop/{id}/update`에서 쇼핑몰의 주인은 쇼핑몰에 대한 정보를 수정할 수 있다.

이때 `RequestBody`로 수정할 쇼핑몰의 이름, 설명, 쇼핑몰 분류를 적절하게 전달해야 한다.

```json
{
	"name": "Example Shop",
	"description": "This is an example shop.",
	"shopCategories": ["<값을 입력하세요.>"]
}
```

해당 `url`은 사업자 사용자 이상의 접근이 가능하기 때문에, Postman의 Authorization에 사업자 사용자 이상 등급의 사용자 토큰을 적절하게 입력해야 한다. 쇼핑몰의 주인과 쇼핑몰을 수정하려는 사용자가 일치해야 한다.

### 쇼핑몰 개설 요청하기

`/shop/{id}/submit`에서 쇼핑몰 개설 요청이이 가능하다.

`{id}`로는 `shopId`가 전달되어야 한다.

해당 `url`은 사업자 사용자 이상의 접근이 가능하기 때문에, Postman의 Authorization에 사업자 사용자 이상 등급의 사용자 토큰을 적절하게 입력해야 한다. 쇼핑몰의 주인과 쇼핑몰을 오픈하려는 사용자가 일치해야 한다.

쇼핑몰 개설이 요청된 쇼핑몰의 상태는 `ShopStatus.*SUBMITTED*`으로 설정된다.

### 쇼핑몰 폐쇄 요청하기

`/shop/{id}/close-request`에서 쇼핑몰 주인은 사유를 작성하여 쇼핑몰 폐쇄 요청을 할 수 있다.

`{id}`로는 `shopId`가 전달되어야 한다.

`RequestBody`로 쇼핑몰 폐쇄 사유를 전달해야 한다.

```json
{
    "closeReason": "쇼핑몰 보완이 필요합니다."
}
```

해당 `url`은 사업자 사용자 이상의 접근이 가능하기 때문에, Postman의 Authorization에 사업자 사용자 이상 등급의 사용자 토큰을 적절하게 입력해야 한다. 쇼핑몰의 주인과 쇼핑몰의 폐쇄 요청을 하려는 사용자가 일치해야 한다.

### ReadOne

`/shop/{id}`에서 쇼핑몰에 대한 단순 정보 조회가 가능하다.

`{id}`로는 `shopId`가 전달되어야 한다.

## Shop Admin

### 개설 요청 쇼핑몰 리스트

`/shop/admin`에서 관리자는 개설 신청한 쇼핑몰의 목록을 확인할 수 있다.

해당 `url`은 관리자의 접근만 가능하기 때문에, Postman의 Authorization에 관리자의 토큰을 적절하게 입력해야 한다.

`shop`의 상태가 `SUBMITTED`인 쇼핑몰의 목록을 확인할 수 있다.


### 쇼핑몰 개설 허가 / 거절

`/shop/admin/{id}/accept`에서 관리자는 쇼핑몰의 개설을 허가하거나 개설을 거절할 수 있다.

`{id}`로는 `shopId`가 전달되어야 한다.

`RequestParam`으로 `?flag=true` 또는 `?flag=false`를 전달해 쇼핑몰 개설의 허락이나 거절이 가능하다.

쇼핑몰의 개설 승인은 `shop`의 상태가 `SUBMIT`인 경우만 승인 가능하다.

해당 `url`은 관리자의 접근만 가능하기 때문에, Postman의 Authorization에 관리자의 토큰을 적절하게 입력해야 한다.

- `?flag=true`: 쇼핑몰의 개설이 승인된 경우

  개설이 승인된 쇼핑몰의 상태는 `OPEN`으로 변경된다.

- `?flag=false`: 쇼핑몰의 개설이 거절된 경우

  쇼핑몰의 개설 거절 사유를 `ReuqestBody`로 함께 전달해야 한다.

    ```json
    {
        "rejectionReason": "거절 사유를 입력하세요."
    }
    ```

  개설이 거절된 쇼핑몰의 상태는 `REJECT`로 변경된다.


### 쇼핑몰 폐쇄 수락

`/shop/admin/{id}/close-accept`에서 관리자는 쇼핑몰의 폐쇄를 수락할 수 있다.

`{id}`로는 `shopId`가 전달되어야 한다.

폐쇄 요청이 온 쇼핑몰만 폐쇄 요청을 수락하거나 거절할 수 있다.

`RequestParam`으로 `?flag=true` 또는 `?flag=false`를 전달해 쇼핑몰 폐쇄 승인이나 거절이 가능하다.

해당 `url`은 관리자의 접근만 가능하기 때문에, Postman의 Authorization에 관리자의 토큰을 적절하게 입력해야 한다.

---

# Product

쇼핑몰의 상품과 관련된 작업을 수행한다.

## Management

쇼핑몰 주인은 쇼핑몰에 상품을 등록하거나 수정, 삭제가 가능하다.

사업자 사용자 이상의 사용자만 접근 가능하다.

### 쇼핑몰 상품 등록하기

`/shop/{shopId}/management/create`에서 쇼핑몰 주인은 쇼핑몰에 상품을 등록할 수 있다.

`RequestBody`로 상품의 이름, 설명, 가격, 재고를 적절하게 전달해야 한다.

```json
{
    "name": "<값을 입력하세요.>",
    "description": "<값을 입력하세요.>",
    "price": 10000,
    "stock": 20
}
```

해당 `url`은 사업자 사용자 이상의 접근이 가능하기 때문에, Postman의 Authorization에 사업자 사용자 이상 등급의 사용자 토큰을 적절하게 입력해야 한다. 쇼핑몰의 주인과 쇼핑몰에 상품을 등록하려는 사용자가 같아야 한다.

### 쇼핑몰 상품 수정하기

`/shop/{shopId}/management/{productId}/update`에서 쇼핑몰 주인은 등록한 상품의 정보를 수정할 수 있다.

`RequestBody`로 수정할 상품의 이름, 설명, 가격, 재고를 적절하게 전달해야 한다.

```json
{
    "name": "<값을 입력하세요.>",
    "description": "<값을 입력하세요.>",
    "price": 10000,
    "stock": 20
}
```

해당 `url`은 사업자 사용자 이상의 접근이 가능하기 때문에, Postman의 Authorization에 사업자 사용자 이상 등급의 사용자 토큰을 적절하게 입력해야 한다. 쇼핑몰의 주인과 쇼핑몰에 등록된 상품을 수정하려는 사용자가 같아야 한다.

### 쇼핑몰 상품 이미지 추가

`/shop/{shopId}/management/{productId}/update`에서 쇼핑몰 주인은 등록한 상품에 이미지를 추가할 수 있다.

`RequestBody`로 `form-data` `Key=image`, `<File>`이 적절하게 전달되어야 한다.

해당 `url`은 사업자 사용자 이상의 접근이 가능하기 때문에, Postman의 Authorization에 사업자 사용자 이상 등급의 사용자 토큰을 적절하게 입력해야 한다. 쇼핑몰의 주인과 상품에 이미지를 추가하려는 사용자가 같아야 한다.

해당 `url`에서 이미지가 업데이트되면 `/media/Product/%d`에 이미지가 저장된다

### 상품 삭제

`/shop/{shopId}/management/{productId}/delete`에서 쇼핑몰 주인은 등록한 상품을 삭제할 수 있다.

해당 `url`은 사업자 사용자 이상의 접근이 가능하기 때문에, Postman의 Authorization에 사업자 사용자 이상 등급의 사용자 토큰을 적절하게 입력해야 한다. 쇼핑몰의 주인과 쇼핑몰의 상품을 삭제하려는 사용자가 같아야 한다.

## Read

비활성 사용자 이상의 사용자만 접근 가능하다.

### ReadAll

`/shop/{shopId}/product`에서 쇼핑몰에 등록된 전체 상품에 대한 단순 조회가 가능하다.

### ReadOne

`/shop/{shopId}/product/{productId}`에서 쇼핑몰에 등록된 상품에 대한 단순 조회가 가능하다.

## Search

비활성 사용자를 제외한 사용자는 쇼핑몰을 조회할 수 있다.

### 쇼핑몰 조회

`/search`에서 조건없이 쇼핑몰을 조회할 수 있다.

조회된 쇼핑몰들은 가장 최근에 거래가 있었던 쇼핑몰 순서로 조회된다.

해당 `url`은 비활성 사용자를 제외한 유저만 접근이 가능하기 때문에, Postman의 Authorization에 비활성 사용자 이상 등급의 사용자 토큰을 적절하게 입력해야 한다.

### 이름 기준으로 쇼핑몰 조회

`/search/shop`에서 이름 기준으로 쇼핑몰 조회가 가능하다.

`RequestParam`으로 `?name=<name>`에 적절한 쇼핑몰의 이름 값을 전달해야 한다.

해당 `name`을 포함하는 쇼핑몰을 조회할 수 있다.

해당 `url`은 비활성 사용자를 제외한 유저만 접근이 가능하기 때문에, Postman의 Authorization에 비활성 사용자 이상 등급의 사용자 토큰을 적절하게 입력해야 한다.

### 쇼핑몰 분류 기준으로 쇼핑몰 조회

`/search/category`에서 쇼핑몰 기준으로 쇼핑몰 조회가 가능하다.

`RequestParam`으로 `?name=<name>`에 적절한 쇼핑몰의 분류 값을 전달해야 한다.

해당 `categoryName`을 포함하는 쇼핑몰을 조회할 수 있다.

해당 `url`은 비활성 사용자를 제외한 유저만 접근이 가능하기 때문에, Postman의 Authorization에 비활성 사용자 이상 등급의 사용자 토큰을 적절하게 입력해야 한다.

### 이름 기준으로 상품 조회

`/search/product`에서 쇼핑몰에 등록된 상품의 이름을 기준으로 상품의 조회가 가능하다.

`RequestParam`으로 `?name=<name>`에 적절한 상품의 이름 값을 전달해야 한다.

해당 `name`을 포함하는 상품 정보가 쇼핑몰의 정보와 함께 제공되어 조회된다.

해당 `url`은 비활성 사용자를 제외한 유저만 접근이 가능하기 때문에, Postman의 Authorization에 비활성 사용자 이상 등급의 사용자 토큰을 적절하게 입력해야 한다.

### 가격 범위를 기준으로 상품 조회

`/search/price`에서 쇼핑몰에 등록된 상품의 가격을 기준으로 상품의 조회가 가능하다.

`RequestParam`으로 `?min=<MIN PRICE>&max=<MAX PRICE>`의 `min`, `max`에 적절한 상품 가격 범위를 전달해야 한다.

해당 `price`범위에 해당하는 상품 정보가 쇼핑몰의 정보와 함께 제공되어 조회된다.

해당 `url`은 비활성 사용자를 제외한 유저만 접근이 가능하기 때문에, Postman의 Authorization에 비활성 사용자 이상 등급의 사용자 토큰을 적절하게 입력해야 한다.

---
# Order

쇼핑몰의 상품 주문을 담당한다.

비활성 사용자를 제외한 사용자는 쇼핑몰의 상품을 구매할 수 있다.

### 구매 요청하기

`/order/shop/{shopId}/product/{productId}/order/create`에서 비활성 사용자를 제외한 사용자는 쇼핑몰의 상품을 구매할 수 있다.

`RequestBody`로 구매할 상품의 적절한 수량을 전달해야 한다.

```json
{
    "count": 3
}
```

해당 `url`은 비활성 사용자를 제외한 유저만 접근이 가능하기 때문에, Postman의 Authorization에 비활성 사용자 이상 등급의 사용자 토큰을 적절하게 입력해야 한다.

`url`에서 요청을 보내면 만들어지는 구매 요청의 상태는 `PREPARING`이다. 이때, 주문할 상품의 재고가 0개인 경우 예외 처리 된다.

### 구매 요청 수락 / 거절

`/order/shop/{shopId}/product/{productId}/order/{orderId}/accept`에서 쇼핑몰의 주인은 구매 요청을 수락하거나 거절할 수 있다.

`RequestParam`으로 `?flag=true` 또는 `?flag=false`를 전달해 구매 요청 승인이나 거절이 가능하다.

- `?flag=true`

  구매 요청이 수락되는 경우,

    ```java
    log.info("total Price: {}", order.getTotalPrice());
    ```

  에서 가격을 확인 할 수 있다.

  구매 요청이 수락되면, 재고 수량이 갱신된다.

  구매 상태는 `ACCEPT`로 변경된다.

- `?flag=false`

  구매 요청이 거절되면 상태는 `REJECT`로 변경된다.


해당 `url`은 사업자 사용자 이상의 접근이 가능하기 때문에, Postman의 Authorization에 사업자 사용자 이상 등급의 사용자 토큰을 적절하게 입력해야 한다. 쇼핑몰의 주인과 주문을 수락하려는 유저가 같아야 한다.

### 구매 취소 요청

`/order/{orderId}/cancel`에서 구매 취소 요청을 할 수 있다.

해당 `url`은 비활성 사용자를 제외한 유저만 접근이 가능하기 때문에, Postman의 Authorization에 비활성 사용자 이상 등급의 사용자 토큰을 적절하게 입력해야 한다. 구매를 취소하려는 사용자와 구매 요청을 보냈던 사용자가 같아야 한다.