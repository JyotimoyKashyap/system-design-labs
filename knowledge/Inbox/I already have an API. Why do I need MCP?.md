---
title: "I already have an API. Why do I need MCP?"
source: "https://aiengg.dev/blog/i-already-have-an-api-why-do-i-need-mcp"
author: "Gaurav Sen"
published: "2026-10-19T20:39:44.000Z"
created: "2026-10-20T13:40:27+05:30"
description: "In this blog we will see what MCP tools are, and how to call them. When the Model Context Protocol (MCP) was launched by Anthropic in 2024,[1] my first reaction was "Why?" I wasn't alone."
tags:
  - clippings
category: "Clipping"
---

When the Model Context Protocol (MCP) was launched by Anthropic in 2024,<sup><a href="https://www.anthropic.com/news/model-context-protocol">[1]</a></sup> my first reaction was " *Why?*"

I wasn't alone. Engineers in our AI engineering cohorts asked about MCP 51 times. One question kept coming back: if it looks like an API, why is it a protocol?

In this blog we will see what MCP tools are, and how to call them.

## 1\. Clients and servers

MCP splits the work between clients and servers. An MCP server sits in front of a service and exposes its APIs. An MCP client runs inside an AI app, such as ChatGPT, and sends the server requests.

The unique thing about this is that you have some sort of intelligence here. A large language model can decide what to do based on the current situation.

![MCP clients in OpenAI's and Google's apps, each joined to the MCP servers of Meta, GitHub and InterviewReady. A call to GitHub's server becomes an action. The panel around the two clients is where the model decides.](https://cdn.aiengg.dev/cohort_blogs/i-already-have-an-api-why-do-i-need-mcp/01-mcp-clients-and-servers.80cd4f01.svg)

MCP clients in OpenAI's and Google's apps, each joined to the MCP servers of Meta, GitHub and InterviewReady. A call to GitHub's server becomes an action. The panel around the two clients is where the model decides.

So the server sends the model a list of tools. The model picks the tool, and the server executes it. It continues this process till the Agent's goal is met.

## 2\. How is this different from an API?

Engineers are well-versed with APIs. Then why do we need a new protocol for Agents?

Because unlike APIs, MCP servers send events when their APIs change. A tool definition like "bookSeat" could be updated in real-time, and all the MCP clients will be notified of this change.

![An MCP server with the tool bookSeat, joined to two MCP clients, OpenAI's and Google's. bookSeat is updated on the server, and the server sends each client an event, tools/list_changed.](https://cdn.aiengg.dev/cohort_blogs/i-already-have-an-api-why-do-i-need-mcp/02-the-server-tells-its-clients.67c60dd6.svg?replay=1)

An MCP server with the tool bookSeat, joined to two MCP clients, OpenAI's and Google's. bookSeat is updated on the server, and the server sends each client an event, tools/list\_changed.

This is possible due to bidirectional Streamable HTTP. MCP internally uses the JSON-RPC 2.0 as its message and communication protocol.

So tool definitions can be changed in real-time, and agents can adapt to a changing environment without requiring restarts.

This also means we don't need to maintain different API versions (thank God!) across different agents. Backward API compatibility isn't easy in the age of AI, so best stay away from it.

## 3\. How does your app learn the tools?

How does your app know what an MCP server offers? It asks the server when it starts.

Technically, there is one difference, which is normally when you have an API client, you have to download the library beforehand, and it clearly defines how to call a function and everything.

But in MCP, you don't need to do that because you are not going to make the function call. Function call is going to be made dynamically by the LLM.

The client sends the server one request, `tools/list`. Back comes the list of tools.<sup><a href="https://modelcontextprotocol.io/specification/2026-07-28/server/tools">[2]</a></sup> Each one has a name, a description and its parameters.

The client hands that list to the model. When the list changes, the server sends `notifications/tools/list_changed`, and the client asks again.

Here is that exchange in Python, with a booking service's MCP server as a dictionary. It doesn't use a network, and keeps the order of the calls.

```python
tools = {"bookSeat": "Book a seat on a flight."}

def server(request):
    """The booking service's MCP server: it answers tools/list with every tool it has."""
    if request["method"] == "tools/list":
        listed = [{"name": name, "description": text} for name, text in tools.items()]
        return {"jsonrpc": "2.0", "id": request["id"], "result": {"tools": listed}}

def list_tools():
    """Our app's MCP client: it asks for the tools, and hands the names to the model."""
    reply = server({"jsonrpc": "2.0", "id": 1, "method": "tools/list"})
    return [(tool["name"], tool["description"]) for tool in reply["result"]["tools"]]

print("when the app starts:", list_tools())

# The service updates bookSeat and adds a tool, and tells its clients the list changed.
tools["bookSeat"] = "Book a seat on a flight, with a seat number."
tools["cancelSeat"] = "Cancel a booked seat."
notice = {"jsonrpc": "2.0", "method": "notifications/tools/list_changed"}

# Our client asks again. Our code didn't change, and nothing was deployed.
if notice["method"] == "notifications/tools/list_changed":
    print("after the notice:", list_tools())
```
```
when the app starts: [('bookSeat', 'Book a seat on a flight.')]
after the notice: [('bookSeat', 'Book a seat on a flight, with a seat number.'), ('cancelSeat', 'Cancel a booked seat.')]
```

So the updated tool and the new one reached the model without a restart.

This MCP server is going to connect with the client, send an event saying that, "I have a new capability added to myself." The client is going to say, "Great."

This is called *runtime discovery*.

## 4\. Is each tool one API?

No. The team that runs the server picks which tools to offer.

What tools do you want to expose is not a one-to-one mapping with the API.

It can be that one tool is actually calling multiple APIs in different order.

So it is wrapped up because LLMs also are prone to failure and don't manage failure very well, unlike developers who can just make a call again.

Some APIs never become tools. A call a person may make can be too risky to hand to a model.

And one tool can run several calls in order. If one fails halfway, the server's code handles it, as a developer would.

## 5\. Can the MCP server read the database?

Could an MCP server skip the API and read the data straight from the database?

Don't do that.

Instead, go through the standard process.

Go to the service which owns that data, make the API call on the MCP server, fetch the data, and then, yes, eventually return that as a response.

Why? The team that owns the data can change how it stores it on any day. A server that reads the tables directly breaks that day.

And if the server writes too, two teams are updating one database. So an MCP server holds wrappers over APIs, and the data stays with its owner.

## So, do I need MCP?

Do you own the API, and does it never change? Then MCP doesn't add much.

If the API contract is not changing, that is a static service. There isn't much of a difference.

Or there may be a team which says that, "I will not create an MCP server. I don't have the bandwidth." So in that case, you can just hit their APIs. You wrap their APIs as tools and then send it to your agent.

MCP pays off when somebody else owns the API. They keep its tools working, and your agent picks up a new one without a deploy.

|  | A tool you wrap | A tool on the owner's MCP server |
| --- | --- | --- |
| Who writes it | Your developer | The API's owner |
| When the model learns it | When you deploy | When the client starts, and on every change |
| A new method | New code and a deploy | A notice from the server |
| A risky method | Left out by your developer | Left out by the owner |

## Summary

1. An MCP server exposes a service's APIs as tools. An MCP client in an AI app calls them, and the model decides which.
2. Unlike an API, an MCP server sends an event when its tools change, and every MCP client is notified.
3. MCP uses JSON-RPC 2.0 for its messages, over bidirectional Streamable HTTP.
4. With MCP, the owner of the API writes the tools.
5. A client asks for the tools with `tools/list` when it starts, and again when the server says the list changed.
6. Tools map to APIs as the owner chooses. Risky APIs stay out, and one tool can make several calls.
7. An MCP server calls the owning service's API, and leaves its database alone.
8. For a static API that one team owns, MCP adds little.

## Sources

1. [Introducing the Model Context Protocol](https://www.anthropic.com/news/model-context-protocol), Anthropic, 25 November 2024.
2. [Tools](https://modelcontextprotocol.io/specification/2026-07-28/server/tools), Model Context Protocol specification, version 2026-07-28.