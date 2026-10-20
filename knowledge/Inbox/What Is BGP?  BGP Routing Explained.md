---
title: "What Is BGP? | BGP Routing Explained"
source: "https://www.cloudflare.com/learning/security/glossary/what-is-bgp/"
author:
published:
created: 2026-10-18
description: "Border Gateway Protocol (BGP) is the routing protocol for the Internet. Much like the post office processing mail, BGP picks the most effecient routes for delivering Internet traffic."
tags:
  - "clippings"
---
## What is BGP?

Border Gateway Protocol (BGP) is the postal service of the Internet. When someone drops a letter into a mailbox, the Postal Service processes that piece of mail and chooses a fast, efficient route to deliver that letter to its recipient. Similarly, when someone submits data via the Internet, BGP is responsible for looking at all of the available paths that data could travel and picking the best route, which usually means hopping between autonomous systems.

BGP is the protocol that makes the Internet work by enabling data routing. When a user in Singapore loads a website with [origin servers](https://www.cloudflare.com/learning/cdn/glossary/origin-server/) in Argentina, BGP is the [protocol](https://www.cloudflare.com/learning/network-layer/what-is-a-protocol/) that enables that communication to happen quickly and efficiently.

## What is an autonomous system?

The Internet is a network of networks. It is broken up into hundreds of thousands of smaller networks known as [autonomous systems (ASes)](https://www.cloudflare.com/learning/network-layer/what-is-an-autonomous-system/). Each of these networks is essentially a large pool of routers run by a single organization.

![Internet topography](https://www.cloudflare.com/img/learning/security/glossary/what-is-bgp/network-of-networks.svg)

Internet topography

If we continue to think of BGP as the Postal Service of the Internet, ASes are like individual post office branches. A town may have hundreds of mailboxes, but the mail in those boxes must go through the local postal branch before being routed to another destination. The internal routers within an AS are like mailboxes. They forward their outbound transmissions to the AS, which then uses BGP routing to get these transmissions to their destinations.

![BGP Simplified](https://www.cloudflare.com/img/learning/security/glossary/what-is-bgp/bgp-simplified.svg)

BGP Simplified

The diagram above illustrates a simplified version of BGP. In this version there are only six ASes on the Internet. If AS1 needs to route a packet to AS3, it has two different options:

Hopping to AS2 and then to AS3:

AS2 → AS3

Or hopping to AS6, then to AS5, AS4, and finally to AS3:

AS6 → AS5 → AS4 → AS3

In this simplified model, the decision seems straightforward. The AS2 route requires fewer hops than the AS6 route, and therefore it is the quickest, most efficient route. Now imagine that there are hundreds of thousands of ASes and that hop count is only one part of a complex route selection algorithm. That is the reality of BGP routing on the Internet.

The structure of the Internet is constantly changing, with new systems popping up and existing systems becoming unavailable. Because of this, every AS must be kept up to date with information regarding new routes as well as obsolete routes. This is done through [peering](https://www.cloudflare.com/learning/network-layer/what-is-peering/) sessions where each AS connects to neighboring ASes with a [TCP/IP](https://www.cloudflare.com/learning/ddos/glossary/tcp-ip/) connection for the purpose of sharing routing information. Using this information, each AS is equipped to properly route outbound data transmissions coming from within.

Here is where part of our analogy falls apart. Unlike post office branches, autonomous systems are not all part of the same organization. In fact, they often belong to competing businesses. For this reason, BGP routes sometimes take business considerations into account. ASes often charge each other to carry traffic across their networks, and the price of access can be factored into which route is ultimately selected.

## Who operates BGP autonomous systems?

ASes typically belong to Internet service providers (ISPs) or other large organizations, such as tech companies, universities, government agencies, and scientific institutions. Each AS wishing to exchange [routing](https://www.cloudflare.com/learning/network-layer/what-is-routing/) information must have a registered autonomous system number (ASN). Internet Assigned Numbers Authority (IANA) assigns ASNs to Regional Internet Registries (RIRs), which then assigns them to ISPs and networks. ASNs are 16 bit numbers between one and 65534 and 32 bit numbers between 131072 and 4294967294. As of 2018, there are approximately 64,000 ASNs in use worldwide. These ASNs are only required for external BGP.

## What is the difference between external BGP and internal BGP?

Routes are exchanged and traffic is transmitted over the Internet using external BGP (eBGP). Autonomous systems can also use an internal version of BGP to route through their internal networks, which is known as internal BGP (iBGP). It should be noted that using internal BGP is NOT a requirement for using external BGP. Autonomous systems can choose from a number of internal protocols to connect the routers on their internal network.

External BGP is like international shipping. There are certain standards and guidelines that need to be followed when shipping a piece of mail internationally. Once that piece of mail reaches its destination country, it has to go through the destination country’s local mail service to reach its final destination. Each country has its own internal mail service that does not necessarily follow the same guidelines as those of other countries. Similarly, each autonomous system can have its own internal routing protocol for routing data within its own network.

## What are BGP attributes?

Overall, BGP tries to find the most efficient path for network traffic. But as noted above, hop count is not the only factor BGP routers use for finding those paths. BGP assigns attributes to each path, and these attributes help routers select a path when there are multiple options. Many routers allow administrators to customize attributes for more granular control over how traffic flows on their networks. Some examples of BGP attributes are:

- Weight: A Cisco-proprietary attribute, this tells a router which local paths are preferred.
- Local preference: This tells a router which outbound path to select.
- Originate: This tells a router to choose routes it added to BGP itself.
- AS path length: Similar to the example diagram above, this attribute tells a router to prefer shorter paths.

There are several other BGP attributes as well. All these attributes are ordered by priority for BGP routers — so that, for example, a BGP router first checks to see which route has the highest weight, then checks local preference, then checks to see if the router originated the route, and so on. (So, if all routes received have an equal weight, the router selects a path based on local preference instead.)

## BGP flaws and how to address them

In 2004, a Turkish ISP called TTNet accidentally advertised incorrect BGP routes to its neighbors. These routes claimed that TTNet itself was the best destination for all traffic on the Internet. As these routes spread further and further to more autonomous systems, a massive disruption occurred, creating a one-day crisis where many people across the world were not able to access some or all of the Internet.

Similarly, in 2008, a Pakistani ISP attempted to use a BGP route to block Pakistani users from visiting YouTube. The ISP then accidentally advertised these routes with its neighboring ASes and the route quickly spread across the Internet’s BGP network. This route sent users trying to access YouTube to a dead end, which resulted in YouTube’s being inaccessible for several hours.

Another incident along these lines occurred in [June 2019](https://blog.cloudflare.com/how-verizon-and-a-bgp-optimizer-knocked-large-parts-of-the-internet-offline-today/), when a small company in Pennsylvania became the preferred path for routes through Verizon's network, causing much of the Internet to become unavailable to users for several hours.

These are examples of a practice called [BGP hijacking](https://www.cloudflare.com/learning/security/glossary/bgp-hijacking/), which does not always happen accidentally. In April 2018, attackers deliberately created bad BGP routes to redirect traffic that was meant for Amazon’s [DNS](https://www.cloudflare.com/learning/dns/what-is-dns/) service. The attackers were able to steal over $100,000 worth of [cryptocurrency](https://www.cloudflare.com/learning/ddos/cryptocurrency-ddos-attacks/) by redirecting the traffic to themselves.

BGP hijacking can be used for several kinds of attacks

- Phishing and social engineering through re-routing users to fake websites
	- [Denial-of-service (DoS)](https://www.cloudflare.com/learning/ddos/glossary/denial-of-service/) through traffic [blackholing](https://www.cloudflare.com/learning/ddos/glossary/ddos-blackhole-routing/) or redirection
		- [On-path attacks](https://www.cloudflare.com/learning/security/threats/on-path-attack/) to modify exchanged data, and subvert reputation-based filtering systems
- Impersonation attacks to eavesdrop on communications

Incidents like these can happen because the route-sharing function of BGP relies on trust, and autonomous systems implicitly trust the routes that are shared with them. When peers announce incorrect route information (intentionally or not), traffic goes where it is not supposed to, potentially with malicious results.

#### How to secure BGP

Fortunately, some progress has been made in securing BGP. Most notably, a security framework for routing called [Resource Public Key Infrastructure (RPKI)](https://blog.cloudflare.com/is-bgp-safe-yet-rpki-routing-security-initiative/) was introduced in 2008. RPKI uses cryptographically signed records called Route Origin Authorization (ROAs) to validate which network operator is allowed to announce an organization’s IP addresses using BGP. This ensures that only authorized parties are announcing an organization’s prefixes.

But RPKI’s existence alone is not enough. If large networks do not follow BGP security best practices, they can spread large-scale hijacking attacks. Currently, [over 50% of the top Internet providers support RPKI](https://isbgpsafeyet.com/) to some extent, but a larger majority is needed to fully secure BGP. Network operators can protect their networks by implementing RPKI and using network alerting technology like [Cloudflare Route Leak Detection](https://blog.cloudflare.com/route-leak-detection/). This feature helps prevent BGP hijacking attacks by letting customers know when unauthorized parties are advertising their prefixes.